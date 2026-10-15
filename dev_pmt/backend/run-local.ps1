Set-Location -Path $PSScriptRoot

# 1. Charge les variables du .env
if (-not (Test-Path .env)) {
    Write-Host "Fichier .env introuvable" -ForegroundColor Red
    exit 1
}
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#=]+)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}
if (-not $env:DB_PASSWORD) {
    Write-Host "DB_PASSWORD absent de .env" -ForegroundColor Red
    exit 1
}

# 2. Arrête toute instance qui occupe le port 8080 (verrouille le jar)
$procIds = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue |
Select-Object -ExpandProperty OwningProcess -Unique
foreach ($procId in $procIds) {
    Write-Host "Arrêt du processus $procId qui occupe le port 8080" -ForegroundColor Yellow
    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
}

# 3. Arrête les java lancés depuis ce projet (verrouillent le jar)
Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
Where-Object { $_.CommandLine -like "*pmtbackend*" } |
ForEach-Object {
    Write-Host "Arrêt du java $($_.ProcessId) lié à pmtbackend" -ForegroundColor Yellow
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep -Seconds 2

# 4. Compile
.\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Échec de la compilation" -ForegroundColor Red
    exit $LASTEXITCODE
}

# 5. Lance l'application en arrière-plan
$app = Start-Process -FilePath "java" `
    -ArgumentList "-jar", "target\pmtbackend-0.0.1-SNAPSHOT.jar" `
    -NoNewWindow -PassThru
Write-Host "Application lancée (PID $($app.Id)), attente du démarrage..." -ForegroundColor Cyan

# 6. Test de santé : attend que le port 8080 écoute (max 60 s)
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    if ($app.HasExited) {
        Write-Host "L'application s'est arrêtée prématurément" -ForegroundColor Red
        exit 1
    }
    if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
        $ready = $true
        break
    }
    Start-Sleep -Seconds 1
}
if (-not $ready) {
    Write-Host "Timeout : le port 8080 n'écoute pas après 60 s" -ForegroundColor Red
    Stop-Process -Id $app.Id -Force -ErrorAction SilentlyContinue
    exit 1
}
Write-Host "Port 8080 ouvert" -ForegroundColor Green

# 7. Test fonctionnel : inscription d'un utilisateur de test
$tmpJson = Join-Path $env:TEMP "pmt_register_test.json"
'{"username":"alice","email":"alice@test.com","password":"motdepasse123"}' |
Out-File -FilePath $tmpJson -Encoding ascii

try {
    $response = Invoke-RestMethod -Method Post `
        -Uri "http://localhost:8080/api/auth/register" `
        -ContentType "application/json" `
        -InFile $tmpJson
    Write-Host "Test d'inscription OK : $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
}
catch {
    $status = $_.Exception.Response.StatusCode.value__
    if ($status -eq 409) {
        Write-Host "Test d'inscription : utilisateur déjà existant (409), comportement attendu" -ForegroundColor Yellow
    } else {
        Write-Host "Test d'inscription KO : $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Application en cours d'exécution (PID $($app.Id)). Ctrl+C pour arrêter." -ForegroundColor Cyan
Wait-Process -Id $app.Id
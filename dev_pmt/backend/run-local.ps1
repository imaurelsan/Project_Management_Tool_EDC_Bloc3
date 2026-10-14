# Charge les variables du fichier .env, construit le jar puis le lance
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#=]+)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}

.\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

java -jar target\pmtbackend-0.0.1-SNAPSHOT.jar
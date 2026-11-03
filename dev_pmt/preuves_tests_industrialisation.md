# Preuves tests et industrialisation

## 1. Backend

Commande exacte demandée :

```powershell
cd dev_pmt\backend
mvn -B clean verify
```

Sortie brute :

```text
mvn : Le terme «mvn» n'est pas reconnu comme nom d'applet de commande, fonction, fichier de script ou programme exécutable.
EXIT_CODE=1
```

Statut : **ECHEC** — Maven n'est pas disponible dans le PATH.

Commande exécutée avec le wrapper :

```powershell
.\mvnw.cmd -B clean verify *> backend-verify.log
```

Sortie brute tronquée :

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Statut wrapper : **OK**.

Artefacts :

```text
target/jacoco.exec: size=554010
target/site/jacoco/jacoco.xml: size=129660
target/site/jacoco/index.html: size=6915
```

Compteurs calculés depuis `target/site/jacoco/jacoco.xml` :

```text
type=INSTRUCTION missed=645 covered=1915 ratio=74.804688%
type=LINE missed=152 covered=447 ratio=74.624374%
```

Rapport HTML : `dev_pmt/backend/target/site/jacoco/index.html`

## 2. Frontend

Commandes :

```powershell
cd dev_pmt\frontend
node -v
npm pkg get engines
npm cache clean --force
npm ci
npm run test:ci
npm run build
```

Sortie brute :

```text
v24.21.0
{}
npm warn using --force Recommended protections disabled.
npm warn tar TAR_ENTRY_ERROR UNKNOWN: unknown error, write
npm warn tar TAR_ENTRY_ERROR EPERM: operation not permitted, write
npm warn tar TAR_ENTRY_ERROR EBADF: bad file descriptor, write
```

Statut `npm ci` : **ECHEC** — erreurs d'écriture sur le chemin synchronisé.

Sortie brute après tentative de tests/build :

```text
Node packages may not be installed. Try installing with 'npm install'.
Error: Could not find the '@angular/build:unit-test' builder's node package.
Error: Could not find the '@angular/build:application' builder's node package.
```

Tests : **ECHEC**, nombre non disponible.  
Couverture : **non générée**.  
Build : **ECHEC**.

Le projet se trouve sous un chemin Google Drive synchronisé. Une copie dans un dossier local non synchronisé est à essayer manuellement.

## 3. CI

Fichier vérifié : `.github/workflows/ci.yml`

```text
java-version: '17'
node-version: '22'
```

`pom.xml` déclare Java 17. `package.json` ne contient pas de champ `engines`; Angular 22 et les dépendances du projet justifient Node 22.

`actionlint` :

```text
actionlint unavailable
```

Statut initial : validation actionlint indisponible lors de la première exécution.

## 4. Industrialisation Podman

Commande :

```powershell
podman compose --env-file .env --env-file .env.local up --build -d
podman compose --env-file .env --env-file .env.local ps
```

Sortie brute :

```text
pmt_backend    Up    0.0.0.0:8080->8080/tcp
pmt_frontend   Up    0.0.0.0:4200->80/tcp
pmt_mysql      Up    0.0.0.0:3306->3306/tcp
```

Logs backend :

```text
HikariPool-1 - Start completed.
Tomcat started on port 8080 with context path '/api'
Started PmtbackendApplication
```

Statut : **OK**.

## 5. API

Avec `X-User-Id: 1` :

```text
HTTP/1.1 200
Content-Type: application/json

[{"id":1,"name":"Refonte site web","description":"Modernisation du site vitrine",...}]
```

Sans en-tête :

```text
HTTP/1.1 400
Content-Type: application/json

{"status":400,"error":"Bad Request","path":"/api/projects"}
```

Via nginx avec `X-User-Id: 1` :

```text
HTTP/1.1 200
Server: nginx/1.31.8
Content-Type: application/json
```

Statut : **OK**.

## 6. Secrets et Git

```powershell
git grep -n root_local_2026
```

```text
git grep: no matches
```

```powershell
git ls-files | Select-String "\.env"
```

```text
dev_pmt/.env
dev_pmt/backend/.env.example
```

Statut : **OK**.

Workflow CI :

```text
.github/workflows/ci.yml
```

Statut : **OK** — fichier présent et validé localement par actionlint.

## Mise à jour des preuves

### Frontend en copie locale hors Google Drive

Copie réalisée avec :

```powershell
robocopy "H:\Mon Drive\Master Expert Ingénierie du Logiciel\3_Bloc Intégration, industrialisation et déploiement de logiciel\Project_Management_Tool_EDC_Bloc3\dev_pmt\frontend" "C:\dev\pmt_frontend_copy" /E /XD node_modules dist coverage
```

Sortie brute :

```text
ROBOCOPY_EXIT=1
```

Commande :

```powershell
cd C:\dev\pmt_frontend_copy
npm ci --no-audit --no-fund
```

Sortie brute :

```text
npm warn tar TAR_BAD_ARCHIVE: Unrecognized archive format
npm warn tarball data for @inquirer/number@https://registry.npmjs.org/@inquirer/number/-/number-4.2.4.tgz seems to be corrupted. Trying again.
npm warn tarball data for @csstools/css-color-parser@https://registry.npmjs.org/@csstools/css-color-parser/-/css-color-parser-4.2.6.tgz seems to be corrupted. Trying again.
npm warn tarball data for @babel/helper-compilation-targets@https://registry.npmjs.org/@babel/helper-compilation-targets/-/helper-compilation-targets-8.0.7.tgz seems to be corrupted. Trying again.
npm warn install-scripts 4 packages had install scripts blocked because they are not covered by allowScripts
```

Statut : **ECHEC** — `npm ci` a échoué ; conformément à la consigne, `npm run test:ci` et `npm run build` n'ont pas été relancés dans cette copie. Aucun nombre de tests ni taux de couverture n'a été ajouté.

### Validation actionlint

Commande :

```powershell
& "C:\Users\imaur\AppData\Local\Microsoft\WinGet\Links\actionlint.exe" ".github/workflows/ci.yml"
```

Sortie brute :

```text
```

Code retour : `0`

Statut : **OK**.

### Relance Podman

Commande :

```powershell
podman compose --env-file .env --env-file .env.local down -v
```

Sortie brute tronquée :

```text
Container pmt_frontend Stopping
Container pmt_frontend Stopped
Container pmt_frontend Removing
Container pmt_frontend Removed
Container pmt_backend Stopping
Container pmt_backend Stopped
Container pmt_backend Removing
Container pmt_backend Removed
Container pmt_mysql Stopping
Container pmt_mysql Stopped
Container pmt_mysql Removing
Container pmt_mysql Removed
Volume dev_pmt_pmt_mysql_data Removing
Volume dev_pmt_pmt_mysql_data Removed
DOWN_EXIT=0
```

Commande :

```powershell
podman compose --env-file .env --env-file .env.local up --build -d
```

Sortie brute :

```text
Container pmt_mysql Healthy
Container pmt_backend Started
Container pmt_frontend Started
UP_EXIT=0
```

### Curls exacts

```powershell
curl.exe -i http://localhost:8080/api/projects
```

```text
HTTP/1.1 400
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 03 Nov 2026 22:49:52 GMT
Connection: close

{"timestamp":"2026-11-03T22:49:52.815+00:00","status":400,"error":"Bad Request","path":"/api/projects"}
```

```powershell
curl.exe -i http://localhost:8080/api/projects -H "X-User-Id: 1"
```

```text
HTTP/1.1 200
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 03 Nov 2026 22:49:53 GMT

[{"id":1,"name":"Refonte site web","description":"Modernisation du site vitrine","startDate":"2026-11-03T22:49:33.460629","creatorId":1,"creatorUsername":"alice"}]
```

```powershell
curl.exe -i http://localhost:4200/api/projects -H "X-User-Id: 1"
```

```text
HTTP/1.1 200
Server: nginx/1.31.8
Date: Tue, 03 Nov 2026 22:49:53 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

[{"id":1,"name":"Refonte site web","description":"Modernisation du site vitrine","startDate":"2026-11-03T22:49:33.460629","creatorId":1,"creatorUsername":"alice"}]
```

```powershell
curl.exe -i http://localhost:4200/api/projects
```

```text
HTTP/1.1 400
Server: nginx/1.31.8
Date: Tue, 03 Nov 2026 22:49:54 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers

{"timestamp":"2026-11-03T22:49:54.253+00:00","status":400,"error":"Bad Request","path":"/api/projects"}
```

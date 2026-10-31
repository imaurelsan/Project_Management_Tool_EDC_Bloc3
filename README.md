# Project_Management_Tool_EDC_Bloc3
Projet de gestion de tâches (PMT) développé avec Angular et Spring Boot dans le cadre d'une étude de cas


## Configuration de la base de données (dev)

Le backend lit ses identifiants MySQL dans les variables d'environnement :

- `DB_URL` (optionnel, défaut : `jdbc:mysql://localhost:3306/pmt_db`)
- `DB_USERNAME` (optionnel, défaut : `pmt_user`)
- `DB_PASSWORD` (obligatoire en dev) : mot de passe de `pmt_user`, à me demander ou à définir localement.

Sous Windows (PowerShell) : `$env:DB_PASSWORD="<votre_password>"`

## Tests et couverture

Depuis `dev_pmt/backend`, lancer `.\mvnw.cmd clean test -Ptest`. Le rapport JaCoCo est disponible dans `target/site/jacoco/index.html`.

## Tests frontend et couverture

Depuis `dev_pmt/frontend`, lancer `npm run test:ci`. Le rapport HTML est disponible dans `coverage/frontend/index.html` et le résumé est affiché dans le terminal.
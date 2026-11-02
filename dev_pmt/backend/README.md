# Backend PMT

API REST faite avec Spring Boot 3.3.5, Java 21, Maven, JPA/Hibernate et MySQL.

## Configuration

Le backend lit ces variables d'environnement :

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/pmt_db"
$env:DB_USERNAME="pmt_user"
$env:DB_PASSWORD="votre_mot_de_passe"
$env:MAIL_HOST="localhost"
$env:MAIL_PORT="1025"
$env:MAIL_FROM="pmt@localhost"
```

`DB_URL` et `DB_USERNAME` ont une valeur par défaut dans `application.properties`. `DB_PASSWORD` doit être défini pour lancer le backend en développement. Les variables `MAIL_*` servent à l'envoi des e-mails d'assignation.

Il n'y a pas de jeton JWT dans le code actuel. L'identifiant de l'utilisateur connecté est envoyé par le frontend dans l'en-tête `X-User-Id`, et le backend s'en sert pour vérifier les droits.

## Base MySQL

```sql
CREATE DATABASE pmt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pmt_user'@'localhost' IDENTIFIED BY 'votre_mot_de_passe';
GRANT ALL PRIVILEGES ON pmt_db.* TO 'pmt_user'@'localhost';
FLUSH PRIVILEGES;
```

La base doit exister avant de démarrer le backend, car Hibernate est réglé sur `ddl-auto=validate` : il vérifie les tables mais ne les crée pas.

## Compiler, lancer et tester

Ces commandes se lancent depuis le dossier `backend` :

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar .\target\pmtbackend-0.0.1-SNAPSHOT.jar
```

Pour lancer les tests :

```powershell
.\mvnw.cmd clean test
```

Le rapport de couverture JaCoCo est généré dans `target\site\jacoco\index.html`.

## Endpoints

Toutes les routes commencent par `/api`. Les routes protégées demandent l'en-tête `X-User-Id`.

| Méthode | Route | Qui peut l'utiliser | Description |
|---|---|---|---|
| POST | `/auth/register` | Tout le monde | Créer un compte |
| POST | `/auth/login` | Tout le monde | Se connecter |
| POST | `/projects` | Utilisateur connecté | Créer un projet |
| GET | `/projects` | Membre | Lister ses projets |
| GET | `/projects/{projectId}` | Membre | Voir un projet |
| PUT | `/projects/{projectId}` | ADMINISTRATOR | Modifier un projet |
| DELETE | `/projects/{projectId}` | ADMINISTRATOR | Supprimer un projet |
| GET | `/projects/{projectId}/members` | Membre | Lister les membres |
| POST | `/projects/{projectId}/members` | ADMINISTRATOR | Ajouter un membre par e-mail |
| PUT | `/projects/{projectId}/members/{memberUserId}` | ADMINISTRATOR | Changer le rôle d'un membre |
| DELETE | `/projects/{projectId}/members/{memberUserId}` | ADMINISTRATOR | Retirer un membre |
| GET | `/projects/{projectId}/tasks` | Membre | Lister les tâches (filtre facultatif `status`) |
| GET | `/projects/{projectId}/tasks/{taskId}` | Membre | Voir une tâche |
| POST | `/projects/{projectId}/tasks` | ADMINISTRATOR, MEMBER | Créer une tâche |
| PUT | `/projects/{projectId}/tasks/{taskId}` | ADMINISTRATOR, MEMBER | Modifier une tâche |
| DELETE | `/projects/{projectId}/tasks/{taskId}` | ADMINISTRATOR | Supprimer une tâche |
| GET | `/projects/{projectId}/tasks/{taskId}/history` | Membre | Voir l'historique d'une tâche |
| GET | `/notifications` | Utilisateur connecté | Lister ses notifications |
| PATCH | `/notifications/{notificationId}/read` | Propriétaire de la notification | Marquer comme lue |

Dans le tableau, « Membre » veut dire n'importe quel membre du projet, quel que soit son rôle.

## Rôles

- `ADMINISTRATOR` (administrateur) : gère le projet, les membres et les suppressions.
- `MEMBER` (membre) : crée et modifie les tâches.
- `VIEWER` (observateur) : peut seulement consulter les projets, les membres, les tâches et les historiques.

## Modèle de données

- `user` : les comptes et leurs informations de connexion
- `project` : les projets et leur propriétaire
- `project_member` : qui appartient à quel projet, avec quel rôle
- `task` : les tâches, avec statut, priorité et personne assignée
- `history` : les modifications d'une tâche, avec l'auteur et la date
- `notification` : les notifications d'un utilisateur, la tâche concernée et l'état de lecture
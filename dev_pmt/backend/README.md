# Backend PMT

API REST faite avec Spring Boot 3.3.5, Java 17, Maven, JPA/Hibernate et MySQL.

## Configuration

En Compose, les variables sont définies dans `dev_pmt/.env` :

```text
MYSQL_ROOT_PASSWORD=...
MYSQL_DATABASE=pmt_db
MYSQL_USER=pmt_user
MYSQL_PASSWORD=...
SPRING_DATASOURCE_USERNAME=pmt_user
SPRING_DATASOURCE_PASSWORD=...
```

Le backend utilise `SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/pmt_db`, ainsi que les variables `SPRING_DATASOURCE_USERNAME` et `SPRING_DATASOURCE_PASSWORD`. Les variables `MAIL_*` servent à l'envoi des e-mails d'assignation.

Il n'y a pas de jeton JWT dans le code actuel. L'identifiant de l'utilisateur connecté est envoyé par le frontend dans l'en-tête `X-User-Id`, et le backend s'en sert pour vérifier les droits.

## Base MySQL

```sql
CREATE DATABASE pmt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pmt_user'@'localhost' IDENTIFIED BY 'votre_mot_de_passe';
GRANT ALL PRIVILEGES ON pmt_db.* TO 'pmt_user'@'localhost';
FLUSH PRIVILEGES;
```

Avec Compose, le service `db` est construit depuis `database/Dockerfile` et exécute `database/init.sql`. Hibernate reste réglé sur `ddl-auto=validate` : il vérifie les tables mais ne les crée pas.

## Compiler, lancer et tester

Depuis `dev_pmt`, lancer l'ensemble :

```powershell
podman compose down -v
podman compose up --build -d
podman compose logs backend --tail=50
```

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
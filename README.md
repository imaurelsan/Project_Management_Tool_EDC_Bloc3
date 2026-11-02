# PMT - Project Management Tool

PMT est un outil de suivi de projets pour de petites équipes. On crée un projet, on invite des collègues avec un rôle, on se répartit des tâches et on garde l'historique de ce qui a été modifié.

Le backend est écrit en Java avec Spring Boot, la base est MySQL et l'interface est faite avec Angular. Ce projet a été réalisé pour le bloc « Intégration, industrialisation et déploiement de logiciel » de l'ISCOD.

## Fonctionnalités

- Créer un compte et se connecter.
- Créer des projets et inviter des membres par e-mail, avec un rôle : administrateur, membre ou observateur.
- Créer, modifier et assigner des tâches, et les filtrer par statut. Un observateur peut seulement consulter.
- Voir l'historique d'une tâche : qui a changé quoi, et quand.
- Recevoir une notification dans l'application (et par e-mail) quand une tâche nous est assignée.
- Suivre l'avancement d'un projet avec un tableau de bord qui compte les tâches par statut.

## Ce qu'il faut avoir installé

- Java 21
- Node.js et npm
- MySQL 8
- PowerShell (les commandes ci-dessous sont écrites pour Windows)
- Facultatif : MailHog, un faux serveur d'e-mails (port 1025), pour tester l'envoi de mails en local. Sans lui, l'application démarre quand même, mais les e-mails ne partent pas.

## Organisation du dépôt

```text
dev_pmt/
|___ backend/    API Spring Boot, accès MySQL, tests Maven
|___ frontend/   Interface Angular, appels HTTP, tests
|___ README.md   Ce fichier
```

Le détail de chaque partie est dans `backend/README.md` et `frontend/README.md`.

## Démarrage rapide

Les étapes sont à faire dans cet ordre.

**1. Créer la base MySQL.** Dans le client MySQL :

```sql
CREATE DATABASE pmt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pmt_user'@'localhost' IDENTIFIED BY 'votre_mot_de_passe';
GRANT ALL PRIVILEGES ON pmt_db.* TO 'pmt_user'@'localhost';
FLUSH PRIVILEGES;
```

**2. Définir les variables d'environnement** dans le terminal où on lancera le backend :

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/pmt_db"
$env:DB_USERNAME="pmt_user"
$env:DB_PASSWORD="votre_mot_de_passe"
```

**3. Compiler et lancer le backend** (il écoute sur http://localhost:8080) :

```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
java -jar .\target\pmtbackend-0.0.1-SNAPSHOT.jar
```

**4. Lancer le frontend** dans un deuxième terminal :

```powershell
cd frontend
npm install
npm start
```

**5. Ouvrir l'application** sur http://localhost:4200.

## Parcours de démonstration

1. Créer un premier compte sur la page d'inscription, puis se connecter.
2. Créer un projet : on en devient l'administrateur.
3. Dans un navigateur en navigation privée, créer un deuxième compte.
4. Revenir sur le premier compte, ouvrir le projet et ajouter le deuxième compte par son e-mail, avec le rôle « membre ».
5. Créer une tâche et l'assigner à ce membre.
6. Sur le deuxième compte, la cloche affiche une notification. Un clic dessus la marque comme lue.
7. Ouvrir la tâche pour voir son historique, puis changer son statut et regarder le tableau de bord.
8. Pour tester le rôle « observateur », changer le rôle du deuxième compte : les boutons de modification disparaissent.

## Problèmes fréquents

- **`spring-boot:run` ne marche pas** : le chemin du projet contient des espaces (Windows). On passe donc par `mvnw.cmd clean package` puis `java -jar`, comme indiqué plus haut.
- **Le port 8080 ou 4200 est déjà pris** : un ancien processus tourne encore. Le fermer avec Ctrl+C dans son terminal, ou redémarrer le terminal.
- **Erreur au démarrage du backend sur la base** : vérifier que `pmt_db` existe, et que `DB_USERNAME` et `DB_PASSWORD` sont définis dans le même terminal que celui qui lance le backend.
- **L'interface reste bloquée ou affiche une vieille version** : arrêter `npm start`, supprimer le dossier `frontend\.angular`, relancer, puis recharger la page avec Ctrl+F5.
- **Une action échoue avec « Une erreur est survenue »** : ouvrir la console du navigateur (F12). Un message CORS signifie que le backend n'a pas été recompilé et relancé après une modification.
- **Les accents sont cassés dans la console MySQL** : taper `SET NAMES utf8mb4;` avant les requêtes.
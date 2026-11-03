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

**1. Configurer l'environnement Docker.** Depuis `dev_pmt`, créer localement le fichier `.env` (il ne doit pas être versionné) :

```text
MYSQL_ROOT_PASSWORD=...
MYSQL_DATABASE=pmt_db
MYSQL_USER=pmt_user
MYSQL_PASSWORD=...
SPRING_DATASOURCE_USERNAME=pmt_user
SPRING_DATASOURCE_PASSWORD=...
```

Le service `db` est construit depuis `database/Dockerfile` et initialise MySQL avec `database/init.sql`.

**2. Lancer les trois services** :

```powershell
podman compose down -v
podman compose up --build -d
```

**3. Vérifier les conteneurs** :

```powershell
podman compose ps
```

**4. Consulter l'API** sur http://localhost:8080/api.

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
- **Erreur au démarrage du backend sur la base** : vérifier les variables `MYSQL_*` et `SPRING_DATASOURCE_*` dans `dev_pmt\.env`, puis recréer le volume avec `podman compose down -v`.
- **L'interface reste bloquée ou affiche une vieille version** : arrêter `npm start`, supprimer le dossier `frontend\.angular`, relancer, puis recharger la page avec Ctrl+F5.
- **Une action échoue avec « Une erreur est survenue »** : ouvrir la console du navigateur (F12). Un message CORS signifie que le backend n'a pas été recompilé et relancé après une modification.
- **Les accents sont cassés dans la console MySQL** : taper `SET NAMES utf8mb4;` avant les requêtes.
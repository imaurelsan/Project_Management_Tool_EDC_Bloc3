# Frontend PMT

Interface faite avec Angular 22. En conteneur, elle appelle l'API via le chemin relatif `/api`, proxyfié par nginx vers le service backend.

## Installation et commandes

Ces commandes se lancent depuis le dossier `frontend` :

```powershell
npm install
npm start
npm run build
npm run test:ci
```

- `npm start` lance l'application sur http://localhost:4200.
- `npm run build` génère la version finale dans `dist\frontend`.
- `npm run test:ci` lance les tests une seule fois et crée un rapport de couverture. Ouvrir le fichier `index.html` du dossier `coverage` pour le lire.
- Avec Compose, l'application est disponible sur http://localhost:4200 et l'API sur http://localhost:8080/api.

## Structure du code

```text
src/app/
├── components/   les pages et les formulaires
├── guards/       protection des pages réservées aux utilisateurs connectés
├── http/         ajoute l'en-tête X-User-Id à chaque requête
├── services/     appels au backend et gestion de la session
└── models.ts     types utilisés dans toute l'application
```

## Pages de l'application

| Route | Qui y accède | Contenu |
|---|---|---|
| `/login` | Tout le monde | Connexion |
| `/register` | Tout le monde | Inscription |
| `/projects` | Utilisateur connecté | Liste des projets et création d'un projet |
| `/projects/:id` | Utilisateur connecté | Membres, rôles, tâches et tableau de bord |
| `/projects/:projectId/tasks/:taskId` | Utilisateur connecté | Détail d'une tâche et son historique |

La barre de navigation affiche le nom de l'utilisateur et la cloche de notifications. L'identifiant de l'utilisateur est gardé dans la session du navigateur, et le frontend l'ajoute à chaque requête vers le backend.
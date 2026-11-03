SET NAMES utf8mb4;

-- Données de démonstration compatibles avec le schéma JPA.
INSERT INTO `user` (id, username, email, password) VALUES
    (1, 'alice', 'alice@test.com', 'motdepasse123'),
    (2, 'benoit', 'benoit@test.com', 'motdepasse123'),
    (3, 'chloe', 'chloe@test.com', 'motdepasse123');

INSERT INTO project (id, name, description, owner_id) VALUES
    (1, 'Refonte site web', 'Modernisation du site vitrine', 1),
    (2, 'Application mobile', 'Développement de l''application mobile', 2);

INSERT INTO project_member (id, role, project_id, user_id) VALUES
    (1, 'ADMINISTRATOR', 1, 1),
    (2, 'MEMBER', 1, 2),
    (3, 'VIEWER', 1, 3),
    (4, 'ADMINISTRATOR', 2, 2),
    (5, 'MEMBER', 2, 3);

INSERT INTO task (id, title, description, priority, status, due_date, project_id, assignee_id) VALUES
    (1, 'Maquetter la page accueil', 'Créer la maquette de la page accueil', 'HAUTE', 'FAIT', '2026-02-10', 1, 2),
    (2, 'Intégrer la page accueil', 'Développer la page accueil responsive', 'HAUTE', 'EN_COURS', '2026-03-15', 1, 2),
    (3, 'Définir les écrans mobiles', 'Lister les écrans principaux', 'MOYEN', 'A_FAIRE', '2026-03-20', 2, 3),
    (4, 'Configurer la connexion utilisateur', 'Ajouter la connexion par e-mail', 'HAUTE', 'A_FAIRE', '2026-04-01', 2, 2),
    (5, 'Rédiger les textes du site', 'Préparer les contenus des pages principales', 'FAIBLE', 'FAIT', '2026-02-05', 1, 1);

INSERT INTO notification (id, message, is_read, task_id, user_id) VALUES
    (1, 'La tâche "Intégrer la page accueil" vous est assignée', FALSE, 2, 2),
    (2, 'Échéance proche pour la tâche "Définir les écrans mobiles"', FALSE, 3, 3),
    (3, 'La tâche "Maquetter la page accueil" est terminée', TRUE, 1, 2);

INSERT INTO history (id, modified_field, old_value, new_value, modified_date, task_id, modified_by) VALUES
    (1, 'priority', 'MOYEN', 'HAUTE', '2026-02-05 09:00:00.000000', 2, 1),
    (2, 'status', 'A_FAIRE', 'EN_COURS', '2026-03-01 10:30:00.000000', 2, 2),
    (3, 'status', 'EN_COURS', 'FAIT', '2026-02-08 17:15:00.000000', 1, 3);

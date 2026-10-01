-- Table user
INSERT INTO `user` (id, username, email, password) VALUES
    (1, 'Alice Martin', 'alice.martin@example.com', 'password-alice'),
    (2, 'Benoit Dupont', 'benoit.dupont@example.com', 'password-benoit'),
    (3, 'Chloe Bernard', 'chloe.bernard@example.com', 'password-chloe');

-- Table project
INSERT INTO project (id, name, description, start_date, created_by) VALUES
    (1, 'Refonte site web', 'Modernisation du site vitrine', '2026-01-15', 1),
    (2, 'Application mobile', 'Developpement de l application mobile', '2026-02-01', 2);

-- Table project_member
INSERT INTO project_member (id, role, project_id, user_id) VALUES
    (1, 'OWNER', 1, 1),
    (2, 'DEVELOPER', 1, 2),
    (3, 'DESIGNER', 1, 3),
    (4, 'OWNER', 2, 2),
    (5, 'DEVELOPER', 2, 3);

-- Table task
INSERT INTO task (id, name, description, priority, status, due_date, end_date, project_id, assigned_to) VALUES
    (1, 'Maquetter la page accueil', 'Créer la maquette de la page d`accueil', 'HIGH', 'DONE', '2026-02-10', '2026-02-08', 1, 3),
    (2, 'Intégrer la page accueil', 'Développer la page d`accueil responsive', 'HIGH', 'IN_PROGRESS', '2026-03-15', NULL, 1, 2),
    (3, 'Définir les écrans mobiles', 'Lister les ecrans principaux de l`application', 'MEDIUM', 'TODO', '2026-03-20', NULL, 2, 3),
    (4, 'Configurer la connexion utilisateur', 'Ajouter la connexion par email', 'HIGH', 'TODO', '2026-04-01', NULL, 2, 2),
    (5, 'Rédiger les textes du site', 'Préparer les contenus des pages principales', 'LOW', 'DONE', '2026-02-05', '2026-02-04', 1, 1);

-- Table notification
INSERT INTO notification (id, message, sent_date, task_id, user_id) VALUES
    (1, 'La tâche "Intégrer la page accueil" vous est assignée', '2026-03-01', 2, 2),
    (2, 'Echéance proche pour la tâche "Définir les écrans mobiles"', '2026-03-²10', 3, 3),
    (3, 'La tâche "Maquetter la page accueil" est terminée', '2026-02-08', 1, 1);

-- Table history
INSERT INTO history (id, modified_field, old_value, new_value, modified_date, task_id, modified_by) VALUES
    (1, 'status', 'TODO', 'IN_PROGRESS', '2026-03-01', 2, 2),
    (2, 'priority', 'MEDIUM', 'HIGH', '2026-02-05', 2, 1),
    (3, 'status', 'IN_PROGRESS', 'DONE', '2026-02-08', 1, 3);

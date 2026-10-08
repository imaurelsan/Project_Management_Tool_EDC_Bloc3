SET NAMES utf8mb4; /* Protège les caractères accentués */

-- Création des tables du projet PMT (Project Management Tool)
-- Les tables sont créées dans l'ordre de leurs dépendances.

-- Table user : les utilisateurs sont créés en premier.
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- Table project : un projet est créé par un utilisateur.
CREATE TABLE project (
    id BIGINT AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    start_date DATE,
    created_by BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_created_by
        FOREIGN KEY (created_by) REFERENCES `user` (id)
);

-- Table project_member : elle relie les utilisateurs aux projets (table de liaison).
CREATE TABLE project_member (
    id BIGINT AUTO_INCREMENT,
    role VARCHAR(50) NOT NULL,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_member_project
        FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_project_member_user
        FOREIGN KEY (user_id) REFERENCES `user` (id)
);

-- Table task : une tâche appartient à un projet et est affectée à un utilisateur.
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    priority VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    due_date DATE,
    end_date DATE,
    project_id BIGINT NOT NULL,
    assigned_to BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_task_project
        FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_task_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES `user` (id)
);

-- Table notification : elle concerne une tâche et un utilisateur.
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT,
    message VARCHAR(255) NOT NULL,
    sent_date DATE,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_task
        FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES `user` (id)
);

-- Table history : c'est l'historique qui garde une trace des modifications faites sur une tâche.
CREATE TABLE history (
    id BIGINT AUTO_INCREMENT,
    modified_field VARCHAR(255) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    modified_date DATE,
    task_id BIGINT NOT NULL,
    modified_by BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_history_task
        FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_history_modified_by
        FOREIGN KEY (modified_by) REFERENCES `user` (id)
);


-- Données de test pour la démonstration
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
    (1, 'Maquetter la page accueil', 'Créer la maquette de la page accueil', 'HIGH', 'DONE', '2026-02-10', '2026-02-08', 1, 3),
    (2, 'Intégrer la page accueil', 'Développer la page accueil responsive', 'HIGH', 'IN_PROGRESS', '2026-03-15', NULL, 1, 2),
    (3, 'Définir les écrans mobiles', 'Lister les écrans principaux de l application', 'MEDIUM', 'TODO', '2026-03-20', NULL, 2, 3),
    (4, 'Configurer la connexion utilisateur', 'Ajouter la connexion par email', 'HIGH', 'TODO', '2026-04-01', NULL, 2, 2),
    (5, 'Rédiger les textes du site', 'Préparer les contenus des pages principales', 'LOW', 'DONE', '2026-02-05', '2026-02-04', 1, 1);

-- Table notification
INSERT INTO notification (id, message, sent_date, task_id, user_id) VALUES
    (1, 'La tâche "Intégrer la page accueil" vous est assignée', '2026-03-01', 2, 2),
    (2, 'Echéance proche pour la tâche "Définir les écrans mobiles"', '2026-03-10', 3, 3),
    (3, 'La tâche "Maquetter la page accueil" est terminée', '2026-02-08', 1, 1);

-- Table history
INSERT INTO history (id, modified_field, old_value, new_value, modified_date, task_id, modified_by) VALUES
    (1, 'status', 'TODO', 'IN_PROGRESS', '2026-03-01', 2, 2),
    (2, 'priority', 'MEDIUM', 'HIGH', '2026-02-05', 2, 1),
    (3, 'status', 'IN_PROGRESS', 'DONE', '2026-02-08', 1, 3);
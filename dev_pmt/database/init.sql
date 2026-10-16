SET NAMES utf8mb4; /* Protège les caractères accentués */

-- Création des tables du projet PMT (Project Management Tool)
-- Les tables sont créées dans l'ordre de leurs dépendances.
-- Les anciennes tables sont supprimées d'abord, dans l'ordre inverse des dépendances.

DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS `user`;

-- Table user : les utilisateurs sont créés en premier.
-- username est unique et sert à l'affichage ; email est unique et sert à la connexion.
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table project : un projet est créé par un utilisateur (created_by).
-- start_date est facultative ; updated_at est mis à jour par l'application.
CREATE TABLE project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    created_by BIGINT NOT NULL,
    CONSTRAINT fk_project_created_by FOREIGN KEY (created_by) REFERENCES `user` (id)
);

-- Table project_member : elle relie les utilisateurs aux projets (table de liaison).
-- Le rôle prend une valeur de l'enum ProjectRole (ADMINISTRATOR, MEMBER, VIEWER).
-- La contrainte uk_project_member empêche qu'un utilisateur soit ajouté deux fois au même projet.
CREATE TABLE project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT uk_project_member UNIQUE (project_id, user_id),
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES `user` (id)
);

-- Table task : une tâche appartient à un projet et peut être affectée à un utilisateur (assigned_to).
-- status et priority prennent les valeurs des enums TaskStatus et TaskPriority.
-- assigned_to est facultatif : une tâche peut être créée sans assignation.
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    due_date DATE,
    end_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    project_id BIGINT NOT NULL,
    assigned_to BIGINT NULL,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_task_assigned_to FOREIGN KEY (assigned_to) REFERENCES `user` (id)
);

-- Table notification : elle concerne une tâche et un utilisateur destinataire.
-- is_read vaut FALSE tant que la notification n'a pas été consultée.
-- Le nom is_read est utilisé car read est un mot réservé en MySQL.
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_notification_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `user` (id)
);

-- Table history : c'est l'historique qui garde une trace des modifications faites sur une tâche.
-- Chaque ligne décrit un champ modifié, son ancienne et sa nouvelle valeur, l'auteur et la date.
CREATE TABLE history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modified_field VARCHAR(100) NOT NULL,
    old_value VARCHAR(500),
    new_value VARCHAR(500),
    modified_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    task_id BIGINT NOT NULL,
    modified_by BIGINT NOT NULL,
    CONSTRAINT fk_history_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_history_modified_by FOREIGN KEY (modified_by) REFERENCES `user` (id)
);


-- ===== Tests pour la démonstration =====

-- Table user : trois utilisateurs de test.
-- Les mots de passe sont en clair pour la démonstration uniquement.
INSERT INTO `user` (id, username, email, password) VALUES
    (1, 'alice', 'alice@test.com', 'motdepasse123'),
    (2, 'benoit', 'benoit@test.com', 'motdepasse123'),
    (3, 'chloe', 'chloe@test.com', 'motdepasse123');

-- Table project : deux projets, créés par Alice et Benoit.
INSERT INTO project (id, name, description, start_date, created_by) VALUES
    (1, 'Refonte site web', 'Modernisation du site vitrine', '2026-01-15', 1),
    (2, 'Application mobile', 'Développement de l''application mobile', '2026-02-01', 2);

-- Table project_member : Alice est administratrice du projet 1, Benoit du projet 2.
INSERT INTO project_member (id, role, project_id, user_id) VALUES
    (1, 'ADMINISTRATOR', 1, 1),
    (2, 'MEMBER', 1, 2),
    (3, 'VIEWER', 1, 3),
    (4, 'ADMINISTRATOR', 2, 2),
    (5, 'MEMBER', 2, 3);

-- Table task : cinq tâches réparties sur les deux projets.
INSERT INTO task (id, name, description, priority, status, due_date, end_date, project_id, assigned_to) VALUES
    (1, 'Maquetter la page accueil', 'Créer la maquette de la page accueil', 'HAUTE', 'FAIT', '2026-02-10', '2026-02-08', 1, 3),
    (2, 'Intégrer la page accueil', 'Développer la page accueil responsive', 'HAUTE', 'EN_COURS', '2026-03-15', NULL, 1, 2),
    (3, 'Définir les écrans mobiles', 'Lister les écrans principaux', 'MOYEN', 'A_FAIRE', '2026-03-20', NULL, 2, 3),
    (4, 'Configurer la connexion utilisateur', 'Ajouter la connexion par e-mail', 'HAUTE', 'A_FAIRE', '2026-04-01', NULL, 2, 2),
    (5, 'Rédiger les textes du site', 'Préparer les contenus des pages principales', 'FAIBLE', 'FAIT', '2026-02-05', '2026-02-04', 1, 1);

-- Table notification : notifications d'assignation et d'échéance de test.
INSERT INTO notification (id, message, is_read, task_id, user_id) VALUES
    (1, 'La tâche "Intégrer la page accueil" vous est assignée', FALSE, 2, 2),
    (2, 'Échéance proche pour la tâche "Définir les écrans mobiles"', FALSE, 3, 3),
    (3, 'La tâche "Maquetter la page accueil" est terminée', TRUE, 1, 1);

-- Table history : trois modifications de test.
INSERT INTO history (id, modified_field, old_value, new_value, modified_date, task_id, modified_by) VALUES
    (1, 'status', 'A_FAIRE', 'EN_COURS', '2026-03-01', 2, 2),
    (2, 'priority', 'MOYEN', 'HAUTE', '2026-02-05', 2, 1),
    (3, 'status', 'EN_COURS', 'FAIT', '2026-02-08', 1, 3);
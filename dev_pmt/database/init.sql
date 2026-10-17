SET NAMES utf8mb4;

-- Script d'initialisation de la base PMT.
-- Exécuté automatiquement par MySQL au premier démarrage d'un volume Docker neuf.
-- Les noms de tables et de colonnes doivent correspondre exactement aux entités JPA
-- (le backend démarre en mode ddl-auto=validate).

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- Utilisateurs (entité User)
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Projets (entité Project) : le créateur est porté par owner_id
CREATE TABLE project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES `user` (id)
);

-- Appartenance aux projets (entité ProjectMember) : un seul couple projet/utilisateur
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

-- Tâches (entité Task) : titre en "title", assignation en "assignee_id"
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    due_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    project_id BIGINT NOT NULL,
    assignee_id BIGINT NULL,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES `user` (id)
);

-- Notifications (entité Notification) : "read" est renommé "is_read"
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_notification_task FOREIGN KEY (task_id) REFERENCES task (id)
);

-- Historique (entité History) : modified_date en DATETIME(6) pour LocalDateTime
CREATE TABLE history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modified_field VARCHAR(100) NOT NULL,
    old_value VARCHAR(500),
    new_value VARCHAR(500),
    modified_date DATETIME(6) NULL,
    task_id BIGINT NOT NULL,
    modified_by BIGINT NOT NULL,
    CONSTRAINT fk_history_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_history_modified_by FOREIGN KEY (modified_by) REFERENCES `user` (id)
);


-- ===== Données de test pour la démonstration =====
-- Mots de passe en clair : uniquement pour la démonstration, jamais en production.

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
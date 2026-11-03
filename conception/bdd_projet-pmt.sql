SET NAMES utf8mb4;

-- Schéma relationnel PMT, aligné sur les entités JPA.
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS `user`;

-- Comptes utilisateurs ; username et email sont uniques.
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

-- Projets ; owner_id désigne le créateur et propriétaire.
CREATE TABLE project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL,
    owner_id BIGINT NOT NULL,
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES `user` (id)
);

-- Membres d'un projet ; un utilisateur ne peut apparaître qu'une fois par projet.
CREATE TABLE project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(255) NOT NULL,
    joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT uk_project_member UNIQUE (project_id, user_id),
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES `user` (id)
);

-- Tâches ; assignee_id est facultatif et référence l'utilisateur assigné.
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(255) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    due_date DATE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL,
    project_id BIGINT NOT NULL,
    assignee_id BIGINT,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES `user` (id)
);

-- Notifications personnelles liées à une tâche.
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_notification_task FOREIGN KEY (task_id) REFERENCES task (id)
);

-- Historique d'une tâche ; modified_date est renseignée automatiquement par JPA.
CREATE TABLE history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modified_field VARCHAR(255) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    modified_date DATETIME(6),
    task_id BIGINT NOT NULL,
    modified_by BIGINT NOT NULL,
    CONSTRAINT fk_history_task FOREIGN KEY (task_id) REFERENCES task (id),
    CONSTRAINT fk_history_modified_by FOREIGN KEY (modified_by) REFERENCES `user` (id)
);

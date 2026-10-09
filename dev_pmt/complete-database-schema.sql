-- Schéma complet de la base PMT, aligné sur les entités JPA du backend.
-- Tables : user, project, project_member, task, notification, history.
-- Compatible MySQL 8.x / MariaDB récente.
-- Exécuter dans la base de données PMT.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `project` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    owner_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_project_owner (owner_id),
    CONSTRAINT fk_project_owner
        FOREIGN KEY (owner_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `project_member` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_project_member_project (project_id),
    KEY idx_project_member_user (user_id),
    CONSTRAINT fk_project_member_project
        FOREIGN KEY (project_id) REFERENCES `project` (id),
    CONSTRAINT fk_project_member_user
        FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `task` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(255) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    due_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    project_id BIGINT NOT NULL,
    assignee_id BIGINT NULL,
    PRIMARY KEY (id),
    KEY idx_task_project (project_id),
    KEY idx_task_assignee (assignee_id),
    CONSTRAINT fk_task_project
        FOREIGN KEY (project_id) REFERENCES `project` (id),
    CONSTRAINT fk_task_assignee
        FOREIGN KEY (assignee_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `notification` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message TEXT NOT NULL,
    `read` BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notification_user (user_id),
    KEY idx_notification_task (task_id),
    KEY idx_notification_created_at (created_at),
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_notification_task
        FOREIGN KEY (task_id) REFERENCES `task` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `history` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    modified_field VARCHAR(255) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    modified_date DATE,
    task_id BIGINT NOT NULL,
    modified_by BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_history_task (task_id),
    KEY idx_history_modified_by (modified_by),
    CONSTRAINT fk_history_task
        FOREIGN KEY (task_id) REFERENCES `task` (id),
    CONSTRAINT fk_history_modified_by
        FOREIGN KEY (modified_by) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

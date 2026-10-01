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
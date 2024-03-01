-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE events_backup (test_id INT, test_column VARCHAR(64000), PRIMARY KEY (test_id));

-- changeset alex:2
ALTER TABLE events_backup RENAME COLUMN test_id to id;
ALTER TABLE events_backup RENAME COLUMN test_column to event_json;

-- changeset alex:3
CREATE TABLE user_account
(
    id         BIGINT  NOT NULL,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    email      VARCHAR(255),
    password   VARCHAR(60),
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_user_account PRIMARY KEY (id)
);

CREATE TABLE role
(
    id   BIGINT NOT NULL,
    name VARCHAR(255),
    resource_id BIGINT,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE users_roles
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

ALTER TABLE user_account
    ADD CONSTRAINT uc_user_account_id UNIQUE (id);

ALTER TABLE users_roles
    ADD CONSTRAINT uc_users_roles_user UNIQUE (user_id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES user_account (id);

CREATE TABLE token
(
    id         BIGINT NOT NULL,
    token      VARCHAR(255),
    user_id    BIGINT NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_token PRIMARY KEY (id)
);

ALTER TABLE token
    ADD CONSTRAINT uc_token_token UNIQUE (token);

ALTER TABLE token
    ADD CONSTRAINT FK_VERIFY_USER FOREIGN KEY (user_id) REFERENCES user_account (id);

INSERT INTO user_account (id, first_name, last_name, email, password, enabled)
VALUES (1, 'A_Test', 'B_Test', 'test@test.com', 'test', true);

INSERT INTO role (id, name) VALUES (1, 'ADMIN');

INSERT INTO users_roles (role_id, user_id) VALUES (1, 1);

INSERT INTO token (id, token, user_id, expiry_date) VALUES (1, 'test', 1, now());
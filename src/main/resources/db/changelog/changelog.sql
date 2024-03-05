-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE events_backup (id BIGINT generated always as identity, event_json VARCHAR(64000),
                            CONSTRAINT pk_events_backup PRIMARY KEY (id));

-- changeset alex:2
CREATE TABLE user_account
(
    id         BIGINT generated always as identity,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    nick_name  VARCHAR(255) NOT NULL default '',
    email      VARCHAR(255),
    password   VARCHAR(60),
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user_account PRIMARY KEY (id)
);

CREATE TABLE role
(
    id   BIGINT generated always as identity,
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
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE;

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE;

CREATE TABLE token
(
    id         BIGINT generated always as identity,
    token      VARCHAR(255),
    user_id    BIGINT NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_token PRIMARY KEY (id)
);

ALTER TABLE token
    ADD CONSTRAINT uc_token_token UNIQUE (token);

ALTER TABLE token
    ADD CONSTRAINT FK_VERIFY_USER FOREIGN KEY (user_id) REFERENCES user_account (id);
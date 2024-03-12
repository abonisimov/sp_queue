-- liquibase formatted sql

-- changeset alex:1
CREATE TABLE events_backup (
    id         BIGINT generated always as identity,
    event_json VARCHAR(64000) NOT NULL,
    CONSTRAINT pk_events_backup PRIMARY KEY (id)
);

-- changeset alex:2
CREATE TABLE user_account
(
    id          BIGINT generated always as identity,
    first_name  VARCHAR(50) NOT NULL,
    last_name   VARCHAR(50) NOT NULL,
    nick_name   VARCHAR(50) NOT NULL default '',
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(60) NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    last_login  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user_account PRIMARY KEY (id)
);

CREATE TABLE role
(
    id          BIGINT generated always as identity,
    name        VARCHAR(25) NOT NULL,
    resource_id BIGINT,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE users_roles
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL
);

ALTER TABLE users_roles ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE;
ALTER TABLE users_roles ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE;
ALTER TABLE users_roles ADD CONSTRAINT uc_users_roles_userId_roleId UNIQUE (user_id, role_id);

CREATE TABLE access_token
(
    id          BIGINT generated always as identity,
    token       VARCHAR(50) NOT NULL,
    user_id     BIGINT NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_access_token PRIMARY KEY (id)
);

ALTER TABLE access_token
    ADD CONSTRAINT uc_access_token_token UNIQUE (token);

ALTER TABLE access_token
    ADD CONSTRAINT fk_access_token_on_user FOREIGN KEY (user_id) REFERENCES user_account (id);

CREATE TABLE restore_password_token
(
    id          BIGINT generated always as identity,
    token       VARCHAR(50) NOT NULL,
    user_id     BIGINT NOT NULL,
    expiry_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_restore_password_token PRIMARY KEY (id)
);

ALTER TABLE restore_password_token
    ADD CONSTRAINT uc_restore_password_token_token UNIQUE (token);

ALTER TABLE restore_password_token
    ADD CONSTRAINT fk_restore_password_token_on_user FOREIGN KEY (user_id) REFERENCES user_account (id);
-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE events_backup (test_id INT, test_column VARCHAR(64000), PRIMARY KEY (test_id))

-- changeset alex:2
ALTER TABLE events_backup RENAME COLUMN test_id to id;
ALTER TABLE events_backup RENAME COLUMN test_column to event_json;
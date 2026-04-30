-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:30:00
-- Service: bail-bond

-- Table: dristi_bail (bailCreatedTime, bailLastModifiedTime)
ALTER TABLE dristi_bail
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ;

UPDATE dristi_bail SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE dristi_bail SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;

ALTER TABLE dristi_bail DROP COLUMN created_time, DROP COLUMN last_modified_time;
ALTER TABLE dristi_bail RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE dristi_bail RENAME COLUMN last_modified_time_ts TO last_modified_time;

-- Table: dristi_bail_document
ALTER TABLE dristi_bail_document
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ;

UPDATE dristi_bail_document SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE dristi_bail_document SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;

ALTER TABLE dristi_bail_document DROP COLUMN created_time, DROP COLUMN last_modified_time;
ALTER TABLE dristi_bail_document RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE dristi_bail_document RENAME COLUMN last_modified_time_ts TO last_modified_time;

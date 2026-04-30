-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:28:00
-- Service: ctc

-- Table: dristi_ctc_applications
ALTER TABLE dristi_ctc_applications
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS date_of_application_approval_ts TIMESTAMPTZ;

UPDATE dristi_ctc_applications SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE dristi_ctc_applications SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;
UPDATE dristi_ctc_applications SET date_of_application_approval_ts = TO_TIMESTAMP(date_of_application_approval / 1000.0) AT TIME ZONE 'UTC' WHERE date_of_application_approval IS NOT NULL;

ALTER TABLE dristi_ctc_applications
    DROP COLUMN created_time,
    DROP COLUMN last_modified_time,
    DROP COLUMN date_of_application_approval;

ALTER TABLE dristi_ctc_applications RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE dristi_ctc_applications RENAME COLUMN last_modified_time_ts TO last_modified_time;
ALTER TABLE dristi_ctc_applications RENAME COLUMN date_of_application_approval_ts TO date_of_application_approval;

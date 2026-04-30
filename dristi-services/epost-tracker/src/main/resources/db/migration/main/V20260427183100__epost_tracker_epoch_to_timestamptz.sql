-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:31:00
-- Service: epost-tracker

ALTER TABLE dristi_epost_tracker
    ADD COLUMN IF NOT EXISTS booking_date_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS received_date_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS status_update_date_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ;

UPDATE dristi_epost_tracker SET booking_date_ts = TO_TIMESTAMP(booking_date / 1000.0) AT TIME ZONE 'UTC' WHERE booking_date IS NOT NULL;
UPDATE dristi_epost_tracker SET received_date_ts = TO_TIMESTAMP(received_date / 1000.0) AT TIME ZONE 'UTC' WHERE received_date IS NOT NULL;
UPDATE dristi_epost_tracker SET status_update_date_ts = TO_TIMESTAMP(status_update_date / 1000.0) AT TIME ZONE 'UTC' WHERE status_update_date IS NOT NULL;
UPDATE dristi_epost_tracker SET created_time_ts = TO_TIMESTAMP(createdTime / 1000.0) AT TIME ZONE 'UTC' WHERE createdTime IS NOT NULL;
UPDATE dristi_epost_tracker SET last_modified_time_ts = TO_TIMESTAMP(lastModifiedTime / 1000.0) AT TIME ZONE 'UTC' WHERE lastModifiedTime IS NOT NULL;

ALTER TABLE dristi_epost_tracker
    DROP COLUMN booking_date,
    DROP COLUMN received_date,
    DROP COLUMN status_update_date,
    DROP COLUMN createdTime,
    DROP COLUMN lastModifiedTime;

ALTER TABLE dristi_epost_tracker RENAME COLUMN booking_date_ts TO booking_date;
ALTER TABLE dristi_epost_tracker RENAME COLUMN received_date_ts TO received_date;
ALTER TABLE dristi_epost_tracker RENAME COLUMN status_update_date_ts TO status_update_date;
ALTER TABLE dristi_epost_tracker RENAME COLUMN created_time_ts TO createdTime;
ALTER TABLE dristi_epost_tracker RENAME COLUMN last_modified_time_ts TO lastModifiedTime;

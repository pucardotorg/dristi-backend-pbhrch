-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:39:00
-- Service: sbi-backend

ALTER TABLE transaction_details
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ;

UPDATE transaction_details SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE transaction_details SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;

ALTER TABLE transaction_details DROP COLUMN created_time, DROP COLUMN last_modified_time;
ALTER TABLE transaction_details RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE transaction_details RENAME COLUMN last_modified_time_ts TO last_modified_time;

-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:27:00
-- Service: payment-calculator-svc

-- Table: POSTAL_HUB
ALTER TABLE POSTAL_HUB
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ;

UPDATE POSTAL_HUB SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE POSTAL_HUB SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;

ALTER TABLE POSTAL_HUB
    DROP COLUMN created_time,
    DROP COLUMN last_modified_time;

ALTER TABLE POSTAL_HUB RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE POSTAL_HUB RENAME COLUMN last_modified_time_ts TO last_modified_time;

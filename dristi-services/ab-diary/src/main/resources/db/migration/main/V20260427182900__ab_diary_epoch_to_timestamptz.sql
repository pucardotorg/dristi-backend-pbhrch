-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:29:00
-- Service: ab-diary

-- Table: dristi_case_diary (diary_date, created_time, last_modified_time)
ALTER TABLE dristi_case_diary
    ADD COLUMN IF NOT EXISTS diary_date_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ;

UPDATE dristi_case_diary SET diary_date_ts = TO_TIMESTAMP(diary_date / 1000.0) AT TIME ZONE 'UTC' WHERE diary_date IS NOT NULL;
UPDATE dristi_case_diary SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE dristi_case_diary SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;

ALTER TABLE dristi_case_diary
    DROP COLUMN diary_date,
    DROP COLUMN created_time,
    DROP COLUMN last_modified_time;

ALTER TABLE dristi_case_diary RENAME COLUMN diary_date_ts TO diary_date;
ALTER TABLE dristi_case_diary RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE dristi_case_diary RENAME COLUMN last_modified_time_ts TO last_modified_time;

-- Table: dristi_case_diary_entry (entry_date, hearingDate, created_time, last_modified_time)
ALTER TABLE dristi_case_diary_entry
    ADD COLUMN IF NOT EXISTS entry_date_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS hearingDate_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ;

UPDATE dristi_case_diary_entry SET entry_date_ts = TO_TIMESTAMP(entry_date / 1000.0) AT TIME ZONE 'UTC' WHERE entry_date IS NOT NULL;
UPDATE dristi_case_diary_entry SET hearingDate_ts = TO_TIMESTAMP(hearingDate / 1000.0) AT TIME ZONE 'UTC' WHERE hearingDate IS NOT NULL;
UPDATE dristi_case_diary_entry SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE dristi_case_diary_entry SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;

ALTER TABLE dristi_case_diary_entry
    DROP COLUMN entry_date,
    DROP COLUMN hearingDate,
    DROP COLUMN created_time,
    DROP COLUMN last_modified_time;

ALTER TABLE dristi_case_diary_entry RENAME COLUMN entry_date_ts TO entry_date;
ALTER TABLE dristi_case_diary_entry RENAME COLUMN hearingDate_ts TO hearingDate;
ALTER TABLE dristi_case_diary_entry RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE dristi_case_diary_entry RENAME COLUMN last_modified_time_ts TO last_modified_time;

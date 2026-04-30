-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:25:00
-- Service: hearing

-- Table: dristi_hearing
ALTER TABLE dristi_hearing
    ADD COLUMN IF NOT EXISTS createdTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastModifiedTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS startTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS endTime_ts TIMESTAMPTZ;

UPDATE dristi_hearing SET createdTime_ts = TO_TIMESTAMP(createdTime / 1000.0) AT TIME ZONE 'UTC' WHERE createdTime IS NOT NULL;
UPDATE dristi_hearing SET lastModifiedTime_ts = TO_TIMESTAMP(lastModifiedTime / 1000.0) AT TIME ZONE 'UTC' WHERE lastModifiedTime IS NOT NULL;
UPDATE dristi_hearing SET startTime_ts = TO_TIMESTAMP(startTime / 1000.0) AT TIME ZONE 'UTC' WHERE startTime IS NOT NULL;
UPDATE dristi_hearing SET endTime_ts = TO_TIMESTAMP(endTime / 1000.0) AT TIME ZONE 'UTC' WHERE endTime IS NOT NULL;

ALTER TABLE dristi_hearing
    DROP COLUMN createdTime,
    DROP COLUMN lastModifiedTime,
    DROP COLUMN startTime,
    DROP COLUMN endTime;

ALTER TABLE dristi_hearing
    RENAME COLUMN createdTime_ts TO createdTime;
ALTER TABLE dristi_hearing
    RENAME COLUMN lastModifiedTime_ts TO lastModifiedTime;
ALTER TABLE dristi_hearing
    RENAME COLUMN startTime_ts TO startTime;
ALTER TABLE dristi_hearing
    RENAME COLUMN endTime_ts TO endTime;

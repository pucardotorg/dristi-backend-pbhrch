-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:42:00
-- Service: casemanagement

-- Table: case_bundle_tracker
ALTER TABLE case_bundle_tracker
    ADD COLUMN IF NOT EXISTS startTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS endTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS createdTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastModifiedTime_ts TIMESTAMPTZ;

UPDATE case_bundle_tracker SET startTime_ts = TO_TIMESTAMP(startTime / 1000.0) AT TIME ZONE 'UTC' WHERE startTime IS NOT NULL;
UPDATE case_bundle_tracker SET endTime_ts = TO_TIMESTAMP(endTime / 1000.0) AT TIME ZONE 'UTC' WHERE endTime IS NOT NULL;
UPDATE case_bundle_tracker SET createdTime_ts = TO_TIMESTAMP(createdTime / 1000.0) AT TIME ZONE 'UTC' WHERE createdTime IS NOT NULL;
UPDATE case_bundle_tracker SET lastModifiedTime_ts = TO_TIMESTAMP(lastModifiedTime / 1000.0) AT TIME ZONE 'UTC' WHERE lastModifiedTime IS NOT NULL;

ALTER TABLE case_bundle_tracker
    DROP COLUMN startTime,
    DROP COLUMN endTime,
    DROP COLUMN createdTime,
    DROP COLUMN lastModifiedTime;

ALTER TABLE case_bundle_tracker RENAME COLUMN startTime_ts TO startTime;
ALTER TABLE case_bundle_tracker RENAME COLUMN endTime_ts TO endTime;
ALTER TABLE case_bundle_tracker RENAME COLUMN createdTime_ts TO createdTime;
ALTER TABLE case_bundle_tracker RENAME COLUMN lastModifiedTime_ts TO lastModifiedTime;

-- Table: case_bundle_bulk_tracker
ALTER TABLE case_bundle_bulk_tracker
    ADD COLUMN IF NOT EXISTS startTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS endTime_ts TIMESTAMPTZ;

UPDATE case_bundle_bulk_tracker SET startTime_ts = TO_TIMESTAMP(startTime / 1000.0) AT TIME ZONE 'UTC' WHERE startTime IS NOT NULL;
UPDATE case_bundle_bulk_tracker SET endTime_ts = TO_TIMESTAMP(endTime / 1000.0) AT TIME ZONE 'UTC' WHERE endTime IS NOT NULL;

ALTER TABLE case_bundle_bulk_tracker DROP COLUMN startTime, DROP COLUMN endTime;
ALTER TABLE case_bundle_bulk_tracker RENAME COLUMN startTime_ts TO startTime;
ALTER TABLE case_bundle_bulk_tracker RENAME COLUMN endTime_ts TO endTime;

-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:26:00
-- Service: task

-- Table: dristi_task
ALTER TABLE dristi_task
    ADD COLUMN IF NOT EXISTS createdTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastModifiedTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS createdDate_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS dateCloseBy_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS dateClosed_ts TIMESTAMPTZ;

UPDATE dristi_task SET createdTime_ts = TO_TIMESTAMP(createdTime / 1000.0) AT TIME ZONE 'UTC' WHERE createdTime IS NOT NULL;
UPDATE dristi_task SET lastModifiedTime_ts = TO_TIMESTAMP(lastModifiedTime / 1000.0) AT TIME ZONE 'UTC' WHERE lastModifiedTime IS NOT NULL;
UPDATE dristi_task SET createdDate_ts = TO_TIMESTAMP(createdDate / 1000.0) AT TIME ZONE 'UTC' WHERE createdDate IS NOT NULL;
UPDATE dristi_task SET dateCloseBy_ts = TO_TIMESTAMP(dateCloseBy / 1000.0) AT TIME ZONE 'UTC' WHERE dateCloseBy IS NOT NULL;
UPDATE dristi_task SET dateClosed_ts = TO_TIMESTAMP(dateClosed / 1000.0) AT TIME ZONE 'UTC' WHERE dateClosed IS NOT NULL;

ALTER TABLE dristi_task
    DROP COLUMN createdTime,
    DROP COLUMN lastModifiedTime,
    DROP COLUMN createdDate,
    DROP COLUMN dateCloseBy,
    DROP COLUMN dateClosed;

ALTER TABLE dristi_task RENAME COLUMN createdTime_ts TO createdTime;
ALTER TABLE dristi_task RENAME COLUMN lastModifiedTime_ts TO lastModifiedTime;
ALTER TABLE dristi_task RENAME COLUMN createdDate_ts TO createdDate;
ALTER TABLE dristi_task RENAME COLUMN dateCloseBy_ts TO dateCloseBy;
ALTER TABLE dristi_task RENAME COLUMN dateClosed_ts TO dateClosed;

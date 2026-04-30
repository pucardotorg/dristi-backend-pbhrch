-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:34:00
-- Service: lock-svc

ALTER TABLE dristi_lock
    ADD COLUMN IF NOT EXISTS lockDate_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lockReleaseTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS createdTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastModifiedTime_ts TIMESTAMPTZ;

UPDATE dristi_lock SET lockDate_ts = TO_TIMESTAMP(lockDate / 1000.0) AT TIME ZONE 'UTC' WHERE lockDate IS NOT NULL;
UPDATE dristi_lock SET lockReleaseTime_ts = TO_TIMESTAMP(lockReleaseTime / 1000.0) AT TIME ZONE 'UTC' WHERE lockReleaseTime IS NOT NULL;
UPDATE dristi_lock SET createdTime_ts = TO_TIMESTAMP(createdTime / 1000.0) AT TIME ZONE 'UTC' WHERE createdTime IS NOT NULL;
UPDATE dristi_lock SET lastModifiedTime_ts = TO_TIMESTAMP(lastModifiedTime / 1000.0) AT TIME ZONE 'UTC' WHERE lastModifiedTime IS NOT NULL;

ALTER TABLE dristi_lock
    DROP COLUMN lockDate,
    DROP COLUMN lockReleaseTime,
    DROP COLUMN createdTime,
    DROP COLUMN lastModifiedTime;

ALTER TABLE dristi_lock RENAME COLUMN lockDate_ts TO lockDate;
ALTER TABLE dristi_lock RENAME COLUMN lockReleaseTime_ts TO lockReleaseTime;
ALTER TABLE dristi_lock RENAME COLUMN createdTime_ts TO createdTime;
ALTER TABLE dristi_lock RENAME COLUMN lastModifiedTime_ts TO lastModifiedTime;

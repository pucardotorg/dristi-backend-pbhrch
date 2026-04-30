-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:32:00
-- Service: Notification

ALTER TABLE dristi_notification
    ADD COLUMN IF NOT EXISTS createdtime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastmodifiedtime_ts TIMESTAMPTZ;

UPDATE dristi_notification SET createdtime_ts = TO_TIMESTAMP(createdtime / 1000.0) AT TIME ZONE 'UTC' WHERE createdtime IS NOT NULL;
UPDATE dristi_notification SET lastmodifiedtime_ts = TO_TIMESTAMP(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC' WHERE lastmodifiedtime IS NOT NULL;

ALTER TABLE dristi_notification DROP COLUMN createdtime, DROP COLUMN lastmodifiedtime;
ALTER TABLE dristi_notification RENAME COLUMN createdtime_ts TO createdtime;
ALTER TABLE dristi_notification RENAME COLUMN lastmodifiedtime_ts TO lastmodifiedtime;

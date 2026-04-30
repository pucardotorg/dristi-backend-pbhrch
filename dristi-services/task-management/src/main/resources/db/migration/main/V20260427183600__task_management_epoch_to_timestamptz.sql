-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:36:00
-- Service: task-management

ALTER TABLE dristi_task_management
    ADD COLUMN IF NOT EXISTS created_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_modified_time_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS createdDate_ts TIMESTAMPTZ;

UPDATE dristi_task_management SET created_time_ts = TO_TIMESTAMP(created_time / 1000.0) AT TIME ZONE 'UTC' WHERE created_time IS NOT NULL;
UPDATE dristi_task_management SET last_modified_time_ts = TO_TIMESTAMP(last_modified_time / 1000.0) AT TIME ZONE 'UTC' WHERE last_modified_time IS NOT NULL;
UPDATE dristi_task_management SET createdDate_ts = TO_TIMESTAMP(createdDate / 1000.0) AT TIME ZONE 'UTC' WHERE createdDate IS NOT NULL;

ALTER TABLE dristi_task_management DROP COLUMN created_time, DROP COLUMN last_modified_time, DROP COLUMN createdDate;
ALTER TABLE dristi_task_management RENAME COLUMN created_time_ts TO created_time;
ALTER TABLE dristi_task_management RENAME COLUMN last_modified_time_ts TO last_modified_time;
ALTER TABLE dristi_task_management RENAME COLUMN createdDate_ts TO createdDate;

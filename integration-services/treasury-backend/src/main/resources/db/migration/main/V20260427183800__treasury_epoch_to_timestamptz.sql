-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:38:00
-- Service: treasury-backend

-- auth_sek_session_data: session_time
ALTER TABLE auth_sek_session_data
    ADD COLUMN IF NOT EXISTS session_time_ts TIMESTAMPTZ;

UPDATE auth_sek_session_data SET session_time_ts = TO_TIMESTAMP(session_time / 1000.0) AT TIME ZONE 'UTC' WHERE session_time IS NOT NULL;

ALTER TABLE auth_sek_session_data DROP COLUMN session_time;
ALTER TABLE auth_sek_session_data RENAME COLUMN session_time_ts TO session_time;

-- treasury_head_breakup_data: createdtime, lastModifiedTime
ALTER TABLE treasury_head_breakup_data
    ADD COLUMN IF NOT EXISTS createdtime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastModifiedTime_ts TIMESTAMPTZ;

UPDATE treasury_head_breakup_data SET createdtime_ts = TO_TIMESTAMP(createdtime / 1000.0) AT TIME ZONE 'UTC' WHERE createdtime IS NOT NULL;
UPDATE treasury_head_breakup_data SET lastModifiedTime_ts = TO_TIMESTAMP(lastModifiedTime / 1000.0) AT TIME ZONE 'UTC' WHERE lastModifiedTime IS NOT NULL;

ALTER TABLE treasury_head_breakup_data DROP COLUMN createdtime, DROP COLUMN lastModifiedTime;
ALTER TABLE treasury_head_breakup_data RENAME COLUMN createdtime_ts TO createdtime;
ALTER TABLE treasury_head_breakup_data RENAME COLUMN lastModifiedTime_ts TO lastModifiedTime;

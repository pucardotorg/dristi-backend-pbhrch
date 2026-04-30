-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27 18:41:00
-- Service: e-sign-svc

ALTER TABLE dristi_esign_pdf
    ADD COLUMN IF NOT EXISTS createdTime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastModifiedTime_ts TIMESTAMPTZ;

UPDATE dristi_esign_pdf SET createdTime_ts = TO_TIMESTAMP(createdTime / 1000.0) AT TIME ZONE 'UTC' WHERE createdTime IS NOT NULL;
UPDATE dristi_esign_pdf SET lastModifiedTime_ts = TO_TIMESTAMP(lastModifiedTime / 1000.0) AT TIME ZONE 'UTC' WHERE lastModifiedTime IS NOT NULL;

ALTER TABLE dristi_esign_pdf DROP COLUMN createdTime, DROP COLUMN lastModifiedTime;
ALTER TABLE dristi_esign_pdf RENAME COLUMN createdTime_ts TO createdTime;
ALTER TABLE dristi_esign_pdf RENAME COLUMN lastModifiedTime_ts TO lastModifiedTime;

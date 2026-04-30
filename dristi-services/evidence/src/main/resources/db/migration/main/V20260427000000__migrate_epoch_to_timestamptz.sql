-- Migration: Convert BIGINT epoch columns to TIMESTAMPTZ
-- Date: 2026-04-27
-- Service: evidence
-- Description: Migrate time columns from BIGINT (epoch millis) to TIMESTAMPTZ

-- ============================================================
-- Table: dristi_evidence_artifact
-- Columns: createdTime, lastModifiedTime, createdDate, publishedDate
-- ============================================================

-- Step 1: Add new TIMESTAMPTZ columns (with temp suffix)
ALTER TABLE dristi_evidence_artifact
    ADD COLUMN IF NOT EXISTS createdtime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS lastmodifiedtime_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS createddate_ts TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS publisheddate_ts TIMESTAMPTZ;

-- Step 2: Backfill from BIGINT epoch millis to TIMESTAMPTZ
UPDATE dristi_evidence_artifact
    SET createdtime_ts = TO_TIMESTAMP(createdtime / 1000.0) AT TIME ZONE 'UTC'
    WHERE createdtime IS NOT NULL;

UPDATE dristi_evidence_artifact
    SET lastmodifiedtime_ts = TO_TIMESTAMP(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC'
    WHERE lastmodifiedtime IS NOT NULL;

UPDATE dristi_evidence_artifact
    SET createddate_ts = TO_TIMESTAMP(createddate / 1000.0) AT TIME ZONE 'UTC'
    WHERE createddate IS NOT NULL;

UPDATE dristi_evidence_artifact
    SET publisheddate_ts = TO_TIMESTAMP(publisheddate / 1000.0) AT TIME ZONE 'UTC'
    WHERE publisheddate IS NOT NULL;

-- Step 3: Drop the old BIGINT columns
ALTER TABLE dristi_evidence_artifact
    DROP COLUMN createdtime,
    DROP COLUMN lastmodifiedtime,
    DROP COLUMN createddate,
    DROP COLUMN publisheddate;

-- Step 4: Rename new TIMESTAMPTZ columns to original names
ALTER TABLE dristi_evidence_artifact
    RENAME COLUMN createdtime_ts TO createdtime;

ALTER TABLE dristi_evidence_artifact
    RENAME COLUMN lastmodifiedtime_ts TO lastmodifiedtime;

ALTER TABLE dristi_evidence_artifact
    RENAME COLUMN createddate_ts TO createddate;

ALTER TABLE dristi_evidence_artifact
    RENAME COLUMN publisheddate_ts TO publisheddate;

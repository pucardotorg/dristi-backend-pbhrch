-- Convert timestamp columns from BIGINT (epoch millis) to TIMESTAMPTZ
-- This migration supports the OffsetDateTime refactoring in the codebase

-- Convert dristi_applications table
ALTER TABLE dristi_applications 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN createddate TYPE TIMESTAMPTZ USING to_timestamp(createddate / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_application_documents table
ALTER TABLE dristi_application_documents 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

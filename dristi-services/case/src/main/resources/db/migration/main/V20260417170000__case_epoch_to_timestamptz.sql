-- Convert timestamp columns from BIGINT (epoch millis) to TIMESTAMPTZ
-- This migration supports the OffsetDateTime refactoring in the codebase

-- Convert dristi_cases table
ALTER TABLE dristi_cases 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_linked_case table
ALTER TABLE dristi_linked_case 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_case_statutes_and_sections table
ALTER TABLE dristi_case_statutes_and_sections 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_case_litigants table
ALTER TABLE dristi_case_litigants 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_case_representatives table
ALTER TABLE dristi_case_representatives 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_case_representing table
ALTER TABLE dristi_case_representing 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_witness table
ALTER TABLE dristi_witness 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_poa table
ALTER TABLE dristi_poa 
  ALTER COLUMN created_time TYPE TIMESTAMPTZ USING to_timestamp(created_time / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN last_modified_time TYPE TIMESTAMPTZ USING to_timestamp(last_modified_time / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_advocate_office_case_member table
ALTER TABLE dristi_advocate_office_case_member 
  ALTER COLUMN created_time TYPE TIMESTAMPTZ USING to_timestamp(created_time / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN last_modified_time TYPE TIMESTAMPTZ USING to_timestamp(last_modified_time / 1000.0) AT TIME ZONE 'UTC';

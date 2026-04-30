-- Convert timestamp columns from BIGINT (epoch millis) to TIMESTAMPTZ
-- This migration supports the OffsetDateTime refactoring in the codebase

-- Convert dristi_orders table
ALTER TABLE dristi_orders 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN createddate TYPE TIMESTAMPTZ USING to_timestamp(createddate / 1000.0) AT TIME ZONE 'UTC';

-- Convert dristi_order_statutes_and_sections table
ALTER TABLE dristi_order_statutes_and_sections 
  ALTER COLUMN createdtime TYPE TIMESTAMPTZ USING to_timestamp(createdtime / 1000.0) AT TIME ZONE 'UTC',
  ALTER COLUMN lastmodifiedtime TYPE TIMESTAMPTZ USING to_timestamp(lastmodifiedtime / 1000.0) AT TIME ZONE 'UTC';

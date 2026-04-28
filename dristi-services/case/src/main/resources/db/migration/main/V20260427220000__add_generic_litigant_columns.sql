ALTER TABLE dristi_case_litigants
-- Add new columns
ADD COLUMN IF NOT EXISTS address_details JSONB,
ADD COLUMN IF NOT EXISTS party_type_detail JSONB,
ADD COLUMN IF NOT EXISTS is_same_address BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS is_joined BOOLEAN DEFAULT TRUE,

-- Modify existing column
ALTER COLUMN mobile_number TYPE JSONB USING CASE
    WHEN mobile_number IS NULL OR btrim(trim(both '"' from mobile_number::text)) = '' THEN NULL
    ELSE to_jsonb(ARRAY[trim(both '"' from mobile_number::text)])
END;

-- Drop old columns (if they exist from previous schema)
DROP COLUMN IF EXISTS company_address;

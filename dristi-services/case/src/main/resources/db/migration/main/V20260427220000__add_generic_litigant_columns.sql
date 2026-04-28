ALTER TABLE dristi_case_litigants
-- Add new columns
ADD COLUMN IF NOT EXISTS address_details JSONB,
ADD COLUMN IF NOT EXISTS party_type_detail JSONB,
ADD COLUMN IF NOT EXISTS is_same_address BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS is_joined BOOLEAN DEFAULT TRUE,

-- Modify existing column
ALTER COLUMN mobile_number TYPE JSONB USING CASE
    WHEN mobile_number IS NULL OR btrim(mobile_number) = '' THEN NULL
    ELSE to_jsonb(ARRAY[mobile_number])
END,

-- Drop old columns (if they exist from previous schema)
DROP COLUMN IF EXISTS company_address;
Drop COLUMN IF EXISTS party_type_detail JSONB;

ALTER TABLE dristi_case_litigants
ADD COLUMN IF NOT EXISTS email JSONB;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'dristi_case_litigants'
          AND column_name = 'complainant_type'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'dristi_case_litigants'
          AND column_name = 'litigant_type'
    ) THEN
        ALTER TABLE dristi_case_litigants RENAME COLUMN complainant_type TO litigant_type;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'dristi_case_litigants'
          AND column_name = 'complainant_type_of_entity'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'dristi_case_litigants'
          AND column_name = 'litigant_type_of_entity'
    ) THEN
        ALTER TABLE dristi_case_litigants RENAME COLUMN complainant_type_of_entity TO litigant_type_of_entity;
    END IF;
END $$;

ALTER TABLE dristi_case_litigants
ADD COLUMN IF NOT EXISTS email JSONB;

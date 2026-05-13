-- patients.tc_no benzersizliğini güçlendir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_patients_tc_no'
    ) THEN
        ALTER TABLE patients
            ADD CONSTRAINT uk_patients_tc_no UNIQUE (tc_no);
    END IF;
END $$;

-- branches.name benzersizliğini güçlendir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_branches_name'
    ) THEN
        ALTER TABLE branches
            ADD CONSTRAINT uk_branches_name UNIQUE (name);
    END IF;
END $$;

-- doctors tablosunda aynı isim+branş tekrarını engelle
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_doctors_name_branch'
    ) THEN
        ALTER TABLE doctors
            ADD CONSTRAINT uk_doctors_name_branch UNIQUE (name, branch_id);
    END IF;
END $$;

-- reports tablo ilişkisi için index
CREATE INDEX IF NOT EXISTS idx_reports_patient_uploaded
    ON reports(patient_id, uploaded_at DESC);

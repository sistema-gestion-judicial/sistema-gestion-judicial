-- Module 3 (Lawsuits): the two-step registration with filing type, observations, party
-- addresses and annex storage, linked to real users and matter codes (DFR 3.1, 3.4, 3.5).

ALTER TABLE demand_requests
  ADD COLUMN matter_id INT NULL AFTER court_code,
  ADD COLUMN filing_type VARCHAR(12) NOT NULL DEFAULT 'INDIVIDUAL' AFTER legal_representative,
  ADD COLUMN observations VARCHAR(1000) NULL AFTER filing_type,
  ADD COLUMN captured_by_user_id BIGINT NULL AFTER captured_by,
  ADD COLUMN has_duplicity_warning BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN duplicity_check_failed BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN rejection_reason_code VARCHAR(20) NULL,
  ADD COLUMN ruled_by_user_id BIGINT NULL,
  ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE demand_requests r JOIN subject_matters m ON m.name = r.matter SET r.matter_id = m.id;
UPDATE demand_requests SET has_duplicity_warning = (duplicity_court IS NOT NULL);
UPDATE demand_requests SET rejection_reason_code = 'OTHER' WHERE status = 'INADMISSIBLE';

ALTER TABLE demand_requests
  MODIFY matter_id INT NOT NULL,
  ADD CONSTRAINT fk_demand_requests_matter_id FOREIGN KEY (matter_id) REFERENCES subject_matters (id),
  ADD CONSTRAINT fk_demand_requests_captured_by FOREIGN KEY (captured_by_user_id) REFERENCES users (id),
  ADD CONSTRAINT fk_demand_requests_ruled_by FOREIGN KEY (ruled_by_user_id) REFERENCES users (id),
  ADD CONSTRAINT fk_demand_requests_rejection FOREIGN KEY (rejection_reason_code) REFERENCES rejection_reasons (code),
  ADD CONSTRAINT ck_demand_requests_filing CHECK (filing_type IN ('INDIVIDUAL', 'COLLECTIVE'));

CREATE INDEX idx_demand_requests_court_status ON demand_requests (court_code, status, received_date);
CREATE INDEX idx_demand_requests_matter ON demand_requests (matter_id);

ALTER TABLE parties
  ADD COLUMN id_type VARCHAR(4) NULL AFTER person_type,
  ADD COLUMN street VARCHAR(150) NULL,
  ADD COLUMN neighborhood VARCHAR(100) NULL,
  ADD COLUMN postal_code CHAR(5) NULL,
  ADD COLUMN municipality VARCHAR(80) NULL;

UPDATE parties SET id_type = CASE WHEN CHAR_LENGTH(person_id) = 18 THEN 'CURP' ELSE 'RFC' END
  WHERE person_id IS NOT NULL;

ALTER TABLE parties
  ADD CONSTRAINT ck_parties_role CHECK (role IN ('PLAINTIFF', 'DEFENDANT')),
  ADD CONSTRAINT ck_parties_id_type CHECK (id_type IS NULL OR id_type IN ('RFC', 'CURP'));

-- The duplicity search compares role + official ID, and role + name when the ID is unknown
DROP INDEX idx_parties_person_id ON parties;
CREATE INDEX idx_parties_role_person_id ON parties (role, person_id);
CREATE INDEX idx_parties_role_name ON parties (role, name);

-- File contents live outside the database (disk or object storage); the row keeps the metadata
ALTER TABLE annexes
  ADD COLUMN storage_key VARCHAR(255) NULL AFTER file_name,
  ADD COLUMN content_type VARCHAR(100) NULL AFTER storage_key,
  ADD COLUMN size_bytes BIGINT UNSIGNED NULL AFTER content_type,
  ADD COLUMN sha256 CHAR(64) NULL,
  ADD COLUMN source VARCHAR(10) NOT NULL DEFAULT 'DIGITAL',
  ADD COLUMN uploaded_by BIGINT NULL,
  ADD COLUMN uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD CONSTRAINT fk_annexes_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id),
  ADD CONSTRAINT ck_annexes_source CHECK (source IN ('DIGITAL', 'SCANNED'));

UPDATE annexes SET size_bytes = size_kb * 1024;

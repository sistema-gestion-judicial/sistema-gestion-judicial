-- Module 3 (Lawsuits): a case file per admissible request, with its lifecycle (active,
-- second instance, concluded), its hearing desk and a yearly sequence per court (DFR 3.5, 3.6).

CREATE TABLE case_files (
  number VARCHAR(13) PRIMARY KEY,
  folio VARCHAR(20) NOT NULL UNIQUE,
  court_code CHAR(2) NOT NULL,
  year SMALLINT UNSIGNED NOT NULL,
  sequence INT UNSIGNED NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  hearing_desk_code VARCHAR(20) NULL,
  hearing_desk_name VARCHAR(60) NULL,
  hearing_desk_pending BOOLEAN NOT NULL DEFAULT TRUE,
  hearing_desk_assigned_at DATETIME NULL,
  first_instance_ruled BOOLEAN NOT NULL DEFAULT FALSE,
  opened_at DATETIME NOT NULL,
  concluded_at DATETIME NULL,
  concluded_by_client_id INT NULL,
  CONSTRAINT fk_case_files_request FOREIGN KEY (folio) REFERENCES demand_requests (folio),
  CONSTRAINT fk_case_files_court FOREIGN KEY (court_code) REFERENCES courts (code),
  CONSTRAINT uq_case_files_sequence UNIQUE (court_code, year, sequence),
  CONSTRAINT ck_case_files_status CHECK (status IN ('ACTIVE', 'SECOND_INSTANCE', 'CONCLUDED')),
  CONSTRAINT ck_case_files_number CHECK (number REGEXP '^[0-9]{4}/[0-9]{5}/[0-9]{2}$')
);

CREATE INDEX idx_case_files_court_status ON case_files (court_code, status);

-- Last sequence used per court and year; read with SELECT ... FOR UPDATE inside the ruling
-- transaction so two clerks never get the same number
CREATE TABLE case_file_sequences (
  court_code CHAR(2) NOT NULL,
  year SMALLINT UNSIGNED NOT NULL,
  last_sequence INT UNSIGNED NOT NULL,
  PRIMARY KEY (court_code, year),
  CONSTRAINT fk_case_file_sequences_court FOREIGN KEY (court_code) REFERENCES courts (code)
);

INSERT INTO case_files (number, folio, court_code, year, sequence, hearing_desk_name, hearing_desk_pending,
                        hearing_desk_assigned_at, opened_at)
  SELECT case_file_number, folio, court_code,
         CAST(SUBSTRING(case_file_number, 1, 4) AS UNSIGNED),
         CAST(SUBSTRING(case_file_number, 6, 5) AS UNSIGNED),
         desk_name, desk_name IS NULL,
         CASE WHEN desk_name IS NULL THEN NULL ELSE COALESCE(ruled_at, received_date) END,
         COALESCE(ruled_at, received_date)
  FROM demand_requests
  WHERE status = 'ADMISSIBLE' AND case_file_number IS NOT NULL;

INSERT INTO case_file_sequences (court_code, year, last_sequence)
  SELECT court_code, year, MAX(sequence) FROM case_files GROUP BY court_code, year;

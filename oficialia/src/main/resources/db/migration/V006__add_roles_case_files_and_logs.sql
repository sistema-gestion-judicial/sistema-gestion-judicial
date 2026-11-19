-- Completes the SCD-2026 schema from the class diagram: roles as a catalog, the decisions taken on a
-- duplicity warning, case files as their own table, and the logs of status changes and of transfers to
-- second instance (DFR 3.1, 3.5 and 3.7). Every table is filled from the data already stored.
-- demand_requests keeps its case file and warning columns until the DAO reads the new tables.

CREATE TABLE roles (
  code VARCHAR(20) PRIMARY KEY,
  name VARCHAR(60) NOT NULL UNIQUE
);

INSERT INTO roles (code, name) VALUES
  ('CLERK', 'Oficial de partes'),
  ('COURT_ADMIN', 'Administrador de juzgado'),
  ('JUDICIAL_BODY_ADMIN', 'Administrador del órgano judicial');

ALTER TABLE users
  ADD CONSTRAINT fk_users_role FOREIGN KEY (role) REFERENCES roles (code);

-- Every duplicity warning shown while registering, with what the clerk decided. A cancelled attempt
-- has no request (folio is null) but keeps its date, user and court (DFR 3.1)
CREATE TABLE duplicity_warning_decisions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  folio VARCHAR(20) NULL,
  court_code CHAR(2) NOT NULL,
  matched_court VARCHAR(120) NOT NULL,
  matched_matter VARCHAR(60) NOT NULL,
  matched_instance VARCHAR(10) NOT NULL DEFAULT 'FIRST',
  decision VARCHAR(10) NOT NULL,
  decided_by VARCHAR(150) NOT NULL,
  decided_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_warning_decisions_request FOREIGN KEY (folio) REFERENCES demand_requests (folio),
  CONSTRAINT fk_warning_decisions_court FOREIGN KEY (court_code) REFERENCES courts (code),
  CONSTRAINT ck_warning_decisions_decision CHECK (decision IN ('CONTINUE', 'CANCEL')),
  CONSTRAINT ck_warning_decisions_instance CHECK (matched_instance IN ('FIRST', 'SECOND')),
  CONSTRAINT ck_warning_decisions_folio CHECK ((decision = 'CONTINUE') = (folio IS NOT NULL))
);

CREATE INDEX idx_warning_decisions_court ON duplicity_warning_decisions (court_code, decided_at);

-- Only the requests registered after a warning were saved before, so all of them continued
INSERT INTO duplicity_warning_decisions
  (folio, court_code, matched_court, matched_matter, decision, decided_by, decided_at)
  SELECT folio, court_code, duplicity_court, COALESCE(duplicity_matter, matter), 'CONTINUE', captured_by,
         received_date
  FROM demand_requests WHERE duplicity_court IS NOT NULL;

-- An admissible request opens one case file, numbered AAAA/NNNNN/JJ per court and year (DFR 3.5).
-- The desk stays null while the hearings service has not assigned it, and the chamber while the
-- case file is in first instance or the appeals service has not answered (DFR 3.7)
CREATE TABLE case_files (
  number VARCHAR(20) PRIMARY KEY,
  folio VARCHAR(20) NOT NULL UNIQUE,
  court_code CHAR(2) NOT NULL,
  year SMALLINT UNSIGNED NOT NULL,
  sequence INT UNSIGNED NOT NULL,
  desk_name VARCHAR(40) NULL,
  instance VARCHAR(10) NOT NULL DEFAULT 'FIRST',
  first_instance_resolved_at DATETIME NULL,
  chamber_code VARCHAR(20) NULL,
  chamber_name VARCHAR(120) NULL,
  opened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_case_files_request FOREIGN KEY (folio) REFERENCES demand_requests (folio),
  CONSTRAINT fk_case_files_court FOREIGN KEY (court_code) REFERENCES courts (code),
  CONSTRAINT uq_case_files_sequence UNIQUE (court_code, year, sequence),
  CONSTRAINT ck_case_files_instance CHECK (instance IN ('FIRST', 'SECOND')),
  CONSTRAINT ck_case_files_chamber CHECK (instance = 'SECOND' OR chamber_code IS NULL)
);

INSERT INTO case_files (number, folio, court_code, year, sequence, desk_name, opened_at)
  SELECT case_file_number, folio, court_code,
         CAST(SUBSTRING_INDEX(case_file_number, '/', 1) AS UNSIGNED),
         CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(case_file_number, '/', 2), '/', -1) AS UNSIGNED),
         desk_name, COALESCE(ruled_at, received_date)
  FROM demand_requests WHERE case_file_number IS NOT NULL;

-- Log of every status a request went through; from_status is null for the registration
CREATE TABLE request_status_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  folio VARCHAR(20) NOT NULL,
  from_status VARCHAR(20) NULL,
  to_status VARCHAR(20) NOT NULL,
  reason VARCHAR(500) NULL,
  changed_by VARCHAR(150) NOT NULL,
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_status_history_request FOREIGN KEY (folio) REFERENCES demand_requests (folio),
  CONSTRAINT ck_status_history_from CHECK (from_status IS NULL OR from_status IN ('PENDING', 'ADMISSIBLE', 'INADMISSIBLE')),
  CONSTRAINT ck_status_history_to CHECK (to_status IN ('PENDING', 'ADMISSIBLE', 'INADMISSIBLE'))
);

CREATE INDEX idx_status_history_request ON request_status_history (folio, changed_at);

INSERT INTO request_status_history (folio, from_status, to_status, changed_by, changed_at)
  SELECT folio, NULL, 'PENDING', captured_by, received_date FROM demand_requests;

INSERT INTO request_status_history (folio, from_status, to_status, reason, changed_by, changed_at)
  SELECT folio, 'PENDING', status, rejection_reason, ruled_by, ruled_at
  FROM demand_requests WHERE status <> 'PENDING' AND ruled_by IS NOT NULL AND ruled_at IS NOT NULL;

-- Log of the transfers to second instance. The chamber is null when the appeals service did not answer,
-- and delivered_at stays null until the case file reaches the appeals system (DFR 3.7)
CREATE TABLE second_instance_transfers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  case_file_number VARCHAR(20) NOT NULL,
  chamber_code VARCHAR(20) NULL,
  chamber_name VARCHAR(120) NULL,
  transferred_by VARCHAR(150) NOT NULL,
  transferred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  chamber_assigned_at DATETIME NULL,
  delivered_at DATETIME NULL,
  CONSTRAINT fk_transfers_case_file FOREIGN KEY (case_file_number) REFERENCES case_files (number)
);

CREATE INDEX idx_transfers_case_file ON second_instance_transfers (case_file_number, transferred_at);

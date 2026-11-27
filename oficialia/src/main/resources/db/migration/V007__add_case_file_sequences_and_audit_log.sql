-- Case file numbers without repeats, and the legal audit trail (DFR 3.5 and 3.3.9, business rule 5).

-- Last sequence used per court and year. Ruling takes the next one with a single upsert inside the ruling
-- transaction, which locks the row, so two clerks of a court never get the same number
CREATE TABLE case_file_sequences (
  court_code CHAR(2) NOT NULL,
  year SMALLINT UNSIGNED NOT NULL,
  last_sequence INT UNSIGNED NOT NULL,
  PRIMARY KEY (court_code, year),
  CONSTRAINT fk_case_file_sequences_court FOREIGN KEY (court_code) REFERENCES courts (code)
);

INSERT INTO case_file_sequences (court_code, year, last_sequence)
  SELECT court_code, year, MAX(sequence) FROM case_files GROUP BY court_code, year;

-- Every act with legal value: who did it (a user, an external system or the system itself), when, on
-- what, and its details. Rows are only added, never changed or deleted.
-- actor_client_id gets its foreign key when the table of API clients arrives with the integration.
CREATE TABLE audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  actor_type VARCHAR(10) NOT NULL,
  actor_user_id BIGINT NULL,
  actor_client_id INT NULL,
  court_code CHAR(2) NULL,
  action VARCHAR(40) NOT NULL,
  entity_type VARCHAR(30) NOT NULL,
  entity_id VARCHAR(40) NOT NULL,
  reason VARCHAR(500) NULL,
  details JSON NULL,
  CONSTRAINT fk_audit_log_user FOREIGN KEY (actor_user_id) REFERENCES users (id),
  CONSTRAINT fk_audit_log_court FOREIGN KEY (court_code) REFERENCES courts (code),
  CONSTRAINT ck_audit_log_actor CHECK (
    (actor_type = 'USER' AND actor_user_id IS NOT NULL)
    OR (actor_type = 'API_CLIENT' AND actor_client_id IS NOT NULL)
    OR actor_type = 'SYSTEM')
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id, occurred_at);
CREATE INDEX idx_audit_log_user ON audit_log (actor_user_id, occurred_at);

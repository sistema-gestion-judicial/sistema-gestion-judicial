-- The legal audit trail (DFR 3.3.9, business rule 5): every act with legal value, who did it
-- (a user, an external system or the system itself), when, on what, and its details.
-- actor_client_id gets its foreign key when the api_clients table arrives with the integration scripts.

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

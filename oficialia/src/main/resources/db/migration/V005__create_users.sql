-- Module 1 (Access control): accounts, roles, login lockout and password recovery
-- (DFR 1.1 to 1.7, 3.3.8). Passwords are stored only as salted hashes.

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  phone VARCHAR(20) NULL,
  role VARCHAR(20) NOT NULL,
  court_code CHAR(2) NULL,
  password_hash VARCHAR(255) NOT NULL,
  must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  failed_attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
  locked_until DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT NULL,
  CONSTRAINT fk_users_court FOREIGN KEY (court_code) REFERENCES courts (code),
  CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT ck_users_role CHECK (role IN ('CLERK', 'COURT_ADMIN', 'JUDICIAL_BODY_ADMIN')),
  -- Clerks and court admins belong to exactly one court; the judiciary admin to none
  CONSTRAINT ck_users_court CHECK ((role = 'JUDICIAL_BODY_ADMIN') = (court_code IS NULL))
);

CREATE INDEX idx_users_court ON users (court_code);

CREATE TABLE password_reset_codes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  code_hash VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  used_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_reset_codes_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_reset_codes_user ON password_reset_codes (user_id, expires_at);

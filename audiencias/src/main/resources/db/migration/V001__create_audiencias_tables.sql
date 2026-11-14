
CREATE TABLE users (
                       id                   INT AUTO_INCREMENT PRIMARY KEY,
                       professional_license VARCHAR(15) NULL,
                       role                 ENUM('ADMINISTRATOR', 'LAWYER') NOT NULL,
                       status               TINYINT(1) NOT NULL DEFAULT 1,
                       email                VARCHAR(150) NOT NULL UNIQUE,
                       password             VARCHAR(255) NOT NULL,
                       full_name            VARCHAR(150) NOT NULL,
                       phone                VARCHAR(15) NULL,
                       created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_email ON users (email);


CREATE TABLE cases (
                       id          INT AUTO_INCREMENT PRIMARY KEY,
                       case_number VARCHAR(20) NOT NULL UNIQUE,
                       court_id    VARCHAR(2) NOT NULL,
                       matter      VARCHAR(30) NOT NULL,
                       defendant   VARCHAR(150) NULL,
                       plaintiff   VARCHAR(150) NULL,
                       status      TINYINT(1) NOT NULL DEFAULT 1,
                       created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       closed_at   DATETIME NULL
);

CREATE INDEX idx_cases_case_number ON cases (case_number);
CREATE INDEX idx_cases_court_matter ON cases (court_id, matter);


CREATE TABLE desks (
                       id        INT AUTO_INCREMENT PRIMARY KEY,
                       desk_name VARCHAR(50) NOT NULL,
                       matter    VARCHAR(30) NOT NULL,
                       active    TINYINT(1) NOT NULL DEFAULT 1,
                       user_id   INT NULL,
                       court_id  VARCHAR(2) NULL,
                       CONSTRAINT fk_desk_lawyer
                           FOREIGN KEY (user_id) REFERENCES users (id)
                               ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_desks_user ON desks (user_id);
CREATE INDEX idx_desks_matter ON desks (matter);
CREATE INDEX idx_desks_court ON desks (court_id);


CREATE TABLE case_files (
                            id          INT AUTO_INCREMENT PRIMARY KEY,
                            case_id     INT NULL,
                            url         VARCHAR(500) NOT NULL,
                            description VARCHAR(200) NULL,
                            uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_case_file_case
                                FOREIGN KEY (case_id) REFERENCES cases (id)
                                    ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_case_files_case ON case_files (case_id);


CREATE TABLE hearings (
                          id           INT AUTO_INCREMENT PRIMARY KEY,
                          hearing_date DATETIME NULL,
                          status       ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
                          desk_id      INT NULL,
                          case_id      INT NULL,
                          lawyer_id    INT NULL,
                          courtroom    VARCHAR(50) NULL,
                          notes        TEXT NULL,
                          CONSTRAINT fk_hearing_lawyer
                              FOREIGN KEY (lawyer_id) REFERENCES users (id)
                                  ON UPDATE CASCADE ON DELETE SET NULL,
                          CONSTRAINT fk_hearing_case
                              FOREIGN KEY (case_id) REFERENCES cases (id)
                                  ON UPDATE CASCADE ON DELETE SET NULL,
                          CONSTRAINT fk_hearing_desk
                              FOREIGN KEY (desk_id) REFERENCES desks (id)
                                  ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_hearings_date ON hearings (hearing_date);
CREATE INDEX idx_hearings_status ON hearings (status);
CREATE INDEX idx_hearings_desk ON hearings (desk_id);
CREATE INDEX idx_hearings_case ON hearings (case_id);
CREATE INDEX idx_hearings_lawyer ON hearings (lawyer_id);


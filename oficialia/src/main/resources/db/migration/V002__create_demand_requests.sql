-- Demand requests with their parties and annexes. An admissible request is a case file.

CREATE TABLE demand_requests (
                                 folio VARCHAR(20) PRIMARY KEY,
                                 received_date DATE NOT NULL,
                                 court_code CHAR(2) NOT NULL,
                                 matter VARCHAR(60) NOT NULL,
                                 legal_representative VARCHAR(150),
                                 captured_by VARCHAR(150) NOT NULL,
                                 duplicity_court VARCHAR(120),
                                 duplicity_matter VARCHAR(60),
                                 duplicity_decision VARCHAR(120),
                                 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                 case_file_number VARCHAR(20) UNIQUE,
                                 desk_name VARCHAR(40),
                                 rejection_reason VARCHAR(500),
                                 ruled_by VARCHAR(150),
                                 ruled_at DATETIME,
                                 CONSTRAINT fk_demand_requests_court FOREIGN KEY (court_code) REFERENCES courts (code),
                                 CONSTRAINT fk_demand_requests_matter FOREIGN KEY (matter) REFERENCES subject_matters (name),
                                 CONSTRAINT ck_demand_requests_status CHECK (status IN ('PENDING', 'ADMISSIBLE', 'INADMISSIBLE'))
);

CREATE INDEX idx_demand_requests_court ON demand_requests (court_code);

CREATE TABLE parties (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         folio VARCHAR(20) NOT NULL,
                         role VARCHAR(20) NOT NULL,
                         name VARCHAR(200),
                         person_type VARCHAR(30),
                         person_id VARCHAR(18),
                         phone VARCHAR(30),
                         email VARCHAR(150),
                         CONSTRAINT fk_parties_request FOREIGN KEY (folio) REFERENCES demand_requests (folio) ON DELETE CASCADE,
                         CONSTRAINT uq_parties_role UNIQUE (folio, role)
);

CREATE INDEX idx_parties_person_id ON parties (person_id);

CREATE TABLE annexes (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         folio VARCHAR(20) NOT NULL,
                         file_name VARCHAR(255) NOT NULL,
                         size_kb INT NOT NULL,
                         CONSTRAINT fk_annexes_request FOREIGN KEY (folio) REFERENCES demand_requests (folio) ON DELETE CASCADE
);

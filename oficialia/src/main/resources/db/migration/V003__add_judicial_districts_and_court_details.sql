-- Module 2 (Catalogs): judicial districts as their own catalog, court address and number,
-- and the subject matters each court handles (DFR 2.1 to 2.4 and 2.9 to 2.12).

CREATE TABLE judicial_districts (
  code VARCHAR(10) PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  seat_municipality VARCHAR(80) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- One district per distinct text value already stored in courts
INSERT INTO judicial_districts (code, name, seat_municipality) VALUES
  ('CVA', 'Distrito judicial de Cuernavaca', 'Cuernavaca'),
  ('YAU', 'Distrito judicial de Yautepec', 'Yautepec'),
  ('JOJ', 'Distrito judicial de Jojutla', 'Jojutla'),
  ('CUA', 'Distrito judicial de Cuautla', 'Cuautla');

-- code stays as the stable key used by the API and foreign keys;
-- number is the official court number printed in the case file number (AAAA/NNNNN/JJ)
ALTER TABLE courts
  ADD COLUMN number TINYINT UNSIGNED NULL AFTER code,
  ADD COLUMN district_code VARCHAR(10) NULL AFTER name,
  ADD COLUMN street VARCHAR(150) NULL,
  ADD COLUMN neighborhood VARCHAR(100) NULL,
  ADD COLUMN postal_code CHAR(5) NULL,
  ADD COLUMN municipality VARCHAR(80) NULL,
  ADD COLUMN phone VARCHAR(20) NULL;

UPDATE courts c
  JOIN judicial_districts d ON d.name = c.judicial_district
  SET c.district_code = d.code, c.number = CAST(c.code AS UNSIGNED), c.municipality = d.seat_municipality;

ALTER TABLE courts
  MODIFY number TINYINT UNSIGNED NOT NULL,
  MODIFY district_code VARCHAR(10) NOT NULL,
  ADD CONSTRAINT uq_courts_number UNIQUE (number),
  ADD CONSTRAINT fk_courts_district FOREIGN KEY (district_code) REFERENCES judicial_districts (code),
  ADD CONSTRAINT ck_courts_postal_code CHECK (postal_code IS NULL OR postal_code REGEXP '^[0-9]{5}$');

CREATE TABLE court_subject_matters (
  court_code CHAR(2) NOT NULL,
  matter_id INT NOT NULL,
  PRIMARY KEY (court_code, matter_id),
  CONSTRAINT fk_court_matters_court FOREIGN KEY (court_code) REFERENCES courts (code),
  CONSTRAINT fk_court_matters_matter FOREIGN KEY (matter_id) REFERENCES subject_matters (id)
);

-- Until the judiciary admin says otherwise, every existing court handles every matter
INSERT INTO court_subject_matters (court_code, matter_id)
  SELECT c.code, m.id FROM courts c CROSS JOIN subject_matters m;

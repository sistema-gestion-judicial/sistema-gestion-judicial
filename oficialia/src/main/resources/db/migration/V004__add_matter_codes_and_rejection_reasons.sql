-- Module 2 (Catalogs): stable matter codes for integration (DFR 2.5 to 2.8, 3.3.6)
-- and the catalog of rejection reasons managed from the database (DFR 3.3.4).

ALTER TABLE subject_matters
  ADD COLUMN code VARCHAR(10) NULL AFTER id,
  ADD COLUMN description VARCHAR(255) NULL AFTER name;

UPDATE subject_matters SET code = CASE name
  WHEN 'Civil' THEN 'CIV'
  WHEN 'Familiar' THEN 'FAM'
  WHEN 'Mercantil' THEN 'MER'
  WHEN 'Penal' THEN 'PEN'
END;

ALTER TABLE subject_matters
  MODIFY code VARCHAR(10) NOT NULL,
  ADD CONSTRAINT uq_subject_matters_code UNIQUE (code);

CREATE TABLE rejection_reasons (
  code VARCHAR(20) PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  requires_notes BOOLEAN NOT NULL DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO rejection_reasons (code, name, requires_notes) VALUES
  ('DUPLICITY', 'Demanda duplicada en otro juzgado', FALSE),
  ('OTHER', 'Otro motivo', TRUE);

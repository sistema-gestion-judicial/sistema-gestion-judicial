-- Module 3 (Requests): step 2 of registering a demand request (DFR 3.1): the address of each party, how
-- the lawsuit was filed, and folios that never repeat.

ALTER TABLE parties
  ADD COLUMN address VARCHAR(255) NULL AFTER person_id;

ALTER TABLE demand_requests
  ADD COLUMN filing_type VARCHAR(20) NULL AFTER legal_representative,
  ADD CONSTRAINT ck_demand_requests_filing_type CHECK (filing_type IS NULL OR filing_type IN ('IN_PERSON', 'MAIL', 'ONLINE'));

-- Last folio number used per year (S-NNNNNN/AAAA). Registering takes the next one with a single upsert inside
-- the registration transaction, which locks the row, so two clerks never get the same folio
CREATE TABLE request_folio_sequences (
  year SMALLINT UNSIGNED PRIMARY KEY,
  last_sequence INT UNSIGNED NOT NULL
);

INSERT INTO request_folio_sequences (year, last_sequence)
  SELECT folio_year, MAX(folio_number)
  FROM (SELECT CAST(SUBSTRING_INDEX(folio, '/', -1) AS UNSIGNED) AS folio_year,
               CAST(SUBSTRING(folio, 3, 6) AS UNSIGNED) AS folio_number
        FROM demand_requests WHERE folio REGEXP '^S-[0-9]{6}/[0-9]{4}$') AS folios
  GROUP BY folio_year;

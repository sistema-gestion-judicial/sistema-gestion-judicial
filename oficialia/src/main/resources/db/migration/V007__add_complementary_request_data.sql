-- Complementary data of the second step of the registration (DFR 3.1): the address of each party, how the
-- demand was presented (by one person or by a group), the clerk's observations and the type of each
-- annex. The requests registered before keep these columns empty and are taken as individual.

ALTER TABLE parties
  ADD COLUMN street VARCHAR(150) NULL,
  ADD COLUMN neighborhood VARCHAR(100) NULL,
  ADD COLUMN postal_code CHAR(5) NULL,
  ADD COLUMN municipality VARCHAR(80) NULL,
  ADD CONSTRAINT ck_parties_postal_code CHECK (postal_code IS NULL OR postal_code REGEXP '^[0-9]{5}$');

ALTER TABLE demand_requests
  ADD COLUMN presentation_type VARCHAR(10) NOT NULL DEFAULT 'INDIVIDUAL',
  ADD COLUMN observations VARCHAR(500) NULL,
  ADD CONSTRAINT ck_demand_requests_presentation CHECK (presentation_type IN ('INDIVIDUAL', 'COLLECTIVE'));

-- Only the type of each annex is added: the files themselves are not kept, because the database of the
-- deployment is too small for them
ALTER TABLE annexes
  ADD COLUMN content_type VARCHAR(150) NULL;

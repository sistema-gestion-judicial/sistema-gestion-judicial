-- Module 2 (Catalogs): renaming a subject matter (DFR 2.7) keeps the demand requests linked to it.
-- demand_requests.matter points to subject_matters.name, so the rename now cascades instead of failing.

ALTER TABLE demand_requests
  DROP FOREIGN KEY fk_demand_requests_matter;

ALTER TABLE demand_requests
  ADD CONSTRAINT fk_demand_requests_matter FOREIGN KEY (matter) REFERENCES subject_matters (name)
    ON UPDATE CASCADE;

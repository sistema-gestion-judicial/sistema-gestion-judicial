-- Module 3 (Case files): the other systems report when the first instance is resolved and when a case file
-- concludes (DFR 3.6); the clerk records when it goes to second instance (DFR 3.7).

ALTER TABLE case_files
  ADD COLUMN second_instance_at DATETIME NULL AFTER chamber_name,
  ADD COLUMN concluded_at DATETIME NULL AFTER second_instance_at;

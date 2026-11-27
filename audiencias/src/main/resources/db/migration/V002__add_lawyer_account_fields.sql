ALTER TABLE users
    ADD COLUMN matter VARCHAR(30) NULL AFTER professional_license,
    ADD COLUMN must_change_password TINYINT(1) NOT NULL DEFAULT 0 AFTER password;

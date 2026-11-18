--liquibase formatted sql

--changeset zahra:004-consents-as-timestamps
-- Every consent is stored as the moment it was given.
-- Terms and privacy are required, the newsletter is optional (null = no consent).
ALTER TABLE booking
    ADD COLUMN privacy_accepted_at    TIMESTAMPTZ NOT NULL,
    ADD COLUMN newsletter_accepted_at TIMESTAMPTZ,
DROP COLUMN newsletter;
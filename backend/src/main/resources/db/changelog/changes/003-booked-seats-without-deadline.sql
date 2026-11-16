--liquibase formatted sql

--changeset zahra:003-booked-seats-without-deadline
-- A booked seat has no deadline: reserved_until is set only while a seat is reserved.
ALTER TABLE seat_reservation
    ALTER COLUMN reserved_until DROP NOT NULL;

ALTER TABLE seat_reservation
    ADD CONSTRAINT chk_reserved_until_matches_status CHECK (
        (status = 'RESERVED' AND reserved_until IS NOT NULL)
            OR (status = 'BOOKED' AND reserved_until IS NULL)
        );
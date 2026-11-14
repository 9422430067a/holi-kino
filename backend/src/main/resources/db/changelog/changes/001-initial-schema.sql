--liquibase formatted sql

--changeset zahra:001-initial-schema
CREATE TABLE movie (
                       id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       title            VARCHAR(200) NOT NULL,
                       duration_minutes INT NOT NULL CHECK (duration_minutes > 0)
);

CREATE TABLE screening (
                           id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           movie_id  BIGINT NOT NULL REFERENCES movie (id),
                           starts_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE booking (
                         id                UUID PRIMARY KEY,
                         full_name         VARCHAR(200) NOT NULL,
                         birth_date        DATE NOT NULL,
                         email             VARCHAR(254) NOT NULL,
                         phone             VARCHAR(50),
                         newsletter        BOOLEAN NOT NULL DEFAULT FALSE,
                         terms_accepted_at TIMESTAMPTZ NOT NULL,
                         created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Sparse model: only reserved or booked seats have a row.
-- A seat without a row, or with an expired RESERVED row, is available.
CREATE TABLE seat_reservation (
                                  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  screening_id      BIGINT NOT NULL REFERENCES screening (id),
                                  seat_row          INT NOT NULL CHECK (seat_row BETWEEN 1 AND 10),
                                  seat_number       INT NOT NULL CHECK (seat_number BETWEEN 1 AND 20),
                                  status            VARCHAR(10) NOT NULL CHECK (status IN ('RESERVED', 'BOOKED')),
                                  reservation_token UUID NOT NULL,
                                  reserved_until    TIMESTAMPTZ NOT NULL,
                                  booking_id        UUID REFERENCES booking (id),

                                  CONSTRAINT uq_seat_per_screening UNIQUE (screening_id, seat_row, seat_number),
                                  CONSTRAINT chk_booked_has_booking CHECK (status <> 'BOOKED' OR booking_id IS NOT NULL)
);

CREATE INDEX idx_seat_reservation_token ON seat_reservation (reservation_token);
package de.zahra.kino.reservation;

import java.util.UUID;

/**
 * The reservation behind a token can no longer be used: it expired,
 * was booked, or was changed by another request in the meantime.
 */
public class ReservationNotActiveException extends RuntimeException {

    public ReservationNotActiveException(UUID token) {
        super("Reservation %s is no longer active".formatted(token));
    }
}
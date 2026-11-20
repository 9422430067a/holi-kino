package de.zahra.kino.reservation;

import java.time.Instant;
import java.util.UUID;

public record ReservationDto(
        UUID reservationToken,
        Long screeningId,
        int seatRow,
        int seatNumber,
        Instant reservedUntil
) {
}
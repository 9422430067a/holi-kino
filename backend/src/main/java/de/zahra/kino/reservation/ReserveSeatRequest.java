package de.zahra.kino.reservation;

import java.util.UUID;

public record ReserveSeatRequest(
        int seatRow,
        int seatNumber,
        UUID token
) {
}
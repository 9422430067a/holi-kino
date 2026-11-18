package de.zahra.kino.reservation;

import java.util.UUID;

public record BookingDto(
        UUID bookingId,
        Long screeningId,
        int seatRow,
        int seatNumber
) {
}
package de.zahra.kino.reservation;

public record SeatDto(
        int seatNumber,
        SeatAvailability status
) {
}
package de.zahra.kino.reservation;

import java.util.List;

public record SeatRowDto(
        int seatRow,
        List<SeatDto> seats
) {
}
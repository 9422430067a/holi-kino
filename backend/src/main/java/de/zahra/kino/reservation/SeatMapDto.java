package de.zahra.kino.reservation;

import java.util.List;

public record SeatMapDto(
        Long screeningId,
        List<SeatRowDto> rows
) {
}
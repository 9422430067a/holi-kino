package de.zahra.kino.screening;

import java.time.Instant;

public record ScreeningDto(
        Long id,
        String movieTitle,
        int durationMinutes,
        Instant startsAt
) {
}
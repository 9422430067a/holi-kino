package de.zahra.kino.screening;

import java.time.Instant;

public record ScreeningDto(
        Long id,
        String movieTitle,
        int durationMinutes,
        Instant startsAt
) {

    public static ScreeningDto from(Screening screening) {
        Movie movie = screening.getMovie();
        return new ScreeningDto(
                screening.getId(),
                movie.getTitle(),
                movie.getDurationMinutes(),
                screening.getStartsAt()
        );
    }
}
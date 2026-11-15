package de.zahra.kino.reservation;

public class ScreeningNotAvailableException extends RuntimeException {

    public ScreeningNotAvailableException(Long screeningId) {
        super("Screening %d does not exist or has already started".formatted(screeningId));
    }
}
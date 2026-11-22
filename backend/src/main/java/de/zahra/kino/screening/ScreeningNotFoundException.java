package de.zahra.kino.screening;

public class ScreeningNotFoundException extends RuntimeException {

    public ScreeningNotFoundException(Long screeningId) {
        super("Screening %d does not exist".formatted(screeningId));
    }
}
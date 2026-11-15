package de.zahra.kino.reservation;

public class SeatAlreadyTakenException extends RuntimeException {

    public SeatAlreadyTakenException(Long screeningId, int seatRow, int seatNumber) {
        super("Seat row %d, number %d is already taken for screening %d"
                .formatted(seatRow, seatNumber, screeningId));
    }
}
package de.zahra.kino.reservation;

public class InvalidSeatException extends RuntimeException {

    public InvalidSeatException(int seatRow, int seatNumber) {
        super("Seat row %d, number %d does not exist (rows 1-%d, seats 1-%d)"
                .formatted(seatRow, seatNumber, Auditorium.ROWS, Auditorium.SEATS_PER_ROW));
    }
}
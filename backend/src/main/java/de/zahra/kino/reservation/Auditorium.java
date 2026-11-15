package de.zahra.kino.reservation;

public final class Auditorium {

    public static final int ROWS = 10;
    public static final int SEATS_PER_ROW = 20;

    private Auditorium() {
        // utility class, no instances
    }

    public static boolean isValidSeat(int seatRow, int seatNumber) {
        return seatRow >= 1 && seatRow <= ROWS
                && seatNumber >= 1 && seatNumber <= SEATS_PER_ROW;
    }
}
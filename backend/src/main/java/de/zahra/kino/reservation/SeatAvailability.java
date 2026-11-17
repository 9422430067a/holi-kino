package de.zahra.kino.reservation;

/**
 * What a guest sees for a seat. Unlike SeatStatus, this includes AVAILABLE,
 * because a free seat has no row in the database.
 */
public enum SeatAvailability {
    AVAILABLE,
    RESERVED,
    BOOKED
}
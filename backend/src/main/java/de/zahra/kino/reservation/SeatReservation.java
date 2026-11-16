package de.zahra.kino.reservation;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seat_reservation")
public class SeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "screening_id", nullable = false)
    private Long screeningId;

    @Column(name = "seat_row", nullable = false)
    private int seatRow;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(name = "reservation_token", nullable = false)
    private UUID reservationToken;

    @Column(name = "reserved_until")
    private Instant reservedUntil;

    @Column(name = "booking_id")
    private UUID bookingId;

    protected SeatReservation() {
        // required by JPA
    }

    public Long getId() {
        return id;
    }

    public Long getScreeningId() {
        return screeningId;
    }

    public int getSeatRow() {
        return seatRow;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public UUID getReservationToken() {
        return reservationToken;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public UUID getBookingId() {
        return bookingId;
    }
}
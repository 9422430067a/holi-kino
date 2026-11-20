package de.zahra.kino.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO seat_reservation
                (screening_id, seat_row, seat_number, status, reservation_token, reserved_until)
            VALUES
                (:screeningId, :seatRow, :seatNumber, 'RESERVED', :reservationToken, :reservedUntil)
            ON CONFLICT (screening_id, seat_row, seat_number)
            DO UPDATE SET
                status = 'RESERVED',
                reservation_token = EXCLUDED.reservation_token,
                reserved_until = EXCLUDED.reserved_until
            WHERE seat_reservation.status = 'RESERVED'
              AND seat_reservation.reserved_until <= :now
            """, nativeQuery = true)
    int reserve(@Param("screeningId") Long screeningId,
                @Param("seatRow") int seatRow,
                @Param("seatNumber") int seatNumber,
                @Param("reservationToken") UUID reservationToken,
                @Param("reservedUntil") Instant reservedUntil,
                @Param("now") Instant now);

    Optional<SeatReservation> findByReservationTokenAndStatusAndReservedUntilAfter(
            UUID reservationToken, SeatStatus status, Instant now);

    @Query("""
            SELECT r FROM SeatReservation r
            WHERE r.screeningId = :screeningId
              AND (r.status = de.zahra.kino.reservation.SeatStatus.BOOKED
                   OR (r.status = de.zahra.kino.reservation.SeatStatus.RESERVED
                       AND r.reservedUntil > :now))
            """)
    List<SeatReservation> findOccupiedSeats(@Param("screeningId") Long screeningId,
                                            @Param("now") Instant now);

    @Modifying
    @Query("""
            DELETE FROM SeatReservation r
            WHERE r.id = :id
              AND r.status = de.zahra.kino.reservation.SeatStatus.RESERVED
            """)
    int deleteReservedById(@Param("id") Long id);

    @Modifying
    @Query("""
            UPDATE SeatReservation r
            SET r.status = de.zahra.kino.reservation.SeatStatus.BOOKED,
                r.reservedUntil = NULL,
                r.bookingId = :bookingId
            WHERE r.reservationToken = :reservationToken
              AND r.status = de.zahra.kino.reservation.SeatStatus.RESERVED
              AND r.reservedUntil > :now
            """)
    int book(@Param("reservationToken") UUID reservationToken,
             @Param("bookingId") UUID bookingId,
             @Param("now") Instant now);

    Optional<SeatReservation> findByBookingId(UUID bookingId);

    @Modifying
    @Query("""
            DELETE FROM SeatReservation r
            WHERE r.status = de.zahra.kino.reservation.SeatStatus.RESERVED
              AND r.reservedUntil <= :now
            """)
    int deleteExpired(@Param("now") Instant now);
}
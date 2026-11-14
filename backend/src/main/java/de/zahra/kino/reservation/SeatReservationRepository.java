package de.zahra.kino.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO seat_reservation
                (screening_id, seat_row, seat_number, status, reservation_token, reserved_until)
            VALUES
                (:screeningId, :seatRow, :seatNumber, 'RESERVED', :token, :reservedUntil)
            ON CONFLICT (screening_id, seat_row, seat_number)
            DO UPDATE SET
                status = 'RESERVED',
                reservation_token = EXCLUDED.reservation_token,
                reserved_until = EXCLUDED.reserved_until
            WHERE seat_reservation.status = 'RESERVED'
              AND seat_reservation.reserved_until < :now
            """, nativeQuery = true)
    int reserve(@Param("screeningId") Long screeningId,
                @Param("seatRow") int seatRow,
                @Param("seatNumber") int seatNumber,
                @Param("token") UUID token,
                @Param("reservedUntil") Instant reservedUntil,
                @Param("now") Instant now);
}
package de.zahra.kino.reservation;

import de.zahra.kino.screening.ScreeningRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService {

    private final SeatReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;
    private final Clock clock;
    private final Duration reservationDuration;

    public ReservationService(SeatReservationRepository reservationRepository,
                              ScreeningRepository screeningRepository,
                              Clock clock,
                              @Value("${kino.reservation.duration:PT5M}") Duration reservationDuration) {
        this.reservationRepository = reservationRepository;
        this.screeningRepository = screeningRepository;
        this.clock = clock;
        this.reservationDuration = reservationDuration;
    }

    @Transactional
    public ReservationDto reserve(Long screeningId, int seatRow, int seatNumber, UUID existingToken) {
        if (!Auditorium.isValidSeat(seatRow, seatNumber)) {
            throw new InvalidSeatException(seatRow, seatNumber);
        }

        Instant now = Instant.now(clock);

        if (!screeningRepository.existsByIdAndStartsAtAfter(screeningId, now)) {
            throw new ScreeningNotAvailableException(screeningId);
        }

        // No token, or an expired, unknown or already booked one: current is empty, so this is a first reservation.
        Optional<SeatReservation> current = findActiveReservation(existingToken, now);

        if (current.isPresent() && isSameSeat(current.get(), screeningId, seatRow, seatNumber)) {
            return toDto(current.get());
        }

        UUID token = current.map(SeatReservation::getReservationToken)
                .orElseGet(UUID::randomUUID);
        Instant reservedUntil = current.map(SeatReservation::getReservedUntil)
                .orElseGet(() -> now.plus(reservationDuration));

        int affectedRows = reservationRepository.reserve(
                screeningId, seatRow, seatNumber, token, reservedUntil, now);

        if (affectedRows == 0) {
            throw new SeatAlreadyTakenException(screeningId, seatRow, seatNumber);
        }

        // For a first reservation current is empty, so nothing happens.
        // Seat change only: release the old seat after the new one was reserved.
        // If no row was deleted, another request changed or booked this reservation
        // in the meantime; the exception rolls back the new seat as well.
        if (current.isPresent()) {
            int deletedRows = reservationRepository.deleteReservedById(current.get().getId());
            if (deletedRows == 0) {
                throw new ReservationNotActiveException(token);
            }
        }

        return new ReservationDto(token, screeningId, seatRow, seatNumber, reservedUntil);
    }

    private Optional<SeatReservation> findActiveReservation(UUID token, Instant now) {
        if (token == null) {
            return Optional.empty();
        }
        return reservationRepository.findByReservationTokenAndStatusAndReservedUntilAfter(
                token, SeatStatus.RESERVED, now);
    }

    private boolean isSameSeat(SeatReservation reservation, Long screeningId, int seatRow, int seatNumber) {
        return reservation.getScreeningId().equals(screeningId)
                && reservation.getSeatRow() == seatRow
                && reservation.getSeatNumber() == seatNumber;
    }

    private ReservationDto toDto(SeatReservation reservation) {
        return new ReservationDto(
                reservation.getReservationToken(),
                reservation.getScreeningId(),
                reservation.getSeatRow(),
                reservation.getSeatNumber(),
                reservation.getReservedUntil());
    }
}
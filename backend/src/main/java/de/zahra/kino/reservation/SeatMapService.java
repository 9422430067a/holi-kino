package de.zahra.kino.reservation;

import de.zahra.kino.screening.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SeatMapService {

    private final SeatReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;
    private final Clock clock;

    public SeatMapService(SeatReservationRepository reservationRepository,
                          ScreeningRepository screeningRepository,
                          Clock clock) {
        this.reservationRepository = reservationRepository;
        this.screeningRepository = screeningRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SeatMapDto getSeatMap(Long screeningId) {
        Instant now = Instant.now(clock);

        if (!screeningRepository.existsByIdAndStartsAtAfter(screeningId, now)) {
            throw new ScreeningNotAvailableException(screeningId);
        }

        // Sparse model: only occupied seats come from the database.
        Map<SeatPosition, SeatAvailability> occupied = reservationRepository
                .findOccupiedSeats(screeningId, now)
                .stream()
                .collect(Collectors.toMap(
                        reservation -> new SeatPosition(reservation.getSeatRow(), reservation.getSeatNumber()),
                        reservation -> toAvailability(reservation.getStatus())));

        List<SeatRowDto> rows = IntStream.rangeClosed(1, Auditorium.ROWS)
                .mapToObj(seatRow -> new SeatRowDto(seatRow, seatsOfRow(seatRow, occupied)))
                .toList();

        return new SeatMapDto(screeningId, rows);
    }

    private List<SeatDto> seatsOfRow(int seatRow, Map<SeatPosition, SeatAvailability> occupied) {
        return IntStream.rangeClosed(1, Auditorium.SEATS_PER_ROW)
                .mapToObj(seatNumber -> new SeatDto(
                        seatNumber,
                        // Everything without an active row is free.
                        occupied.getOrDefault(new SeatPosition(seatRow, seatNumber), SeatAvailability.AVAILABLE)))
                .toList();
    }

    private static SeatAvailability toAvailability(SeatStatus status) {
        return switch (status) {
            case RESERVED -> SeatAvailability.RESERVED;
            case BOOKED -> SeatAvailability.BOOKED;
        };
    }

    private record SeatPosition(int seatRow, int seatNumber) {
    }
}
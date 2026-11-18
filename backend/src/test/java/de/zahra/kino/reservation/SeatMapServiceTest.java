package de.zahra.kino.reservation;

import de.zahra.kino.MutableClock;
import de.zahra.kino.MutableClockConfiguration;
import de.zahra.kino.TestData;
import de.zahra.kino.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static de.zahra.kino.MutableClockConfiguration.START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import({TestcontainersConfiguration.class, MutableClockConfiguration.class, TestData.class})
class SeatMapServiceTest {

    @Autowired
    private SeatMapService seatMapService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MutableClock clock;

    @Autowired
    private TestData testData;

    private Long screeningId;

    @BeforeEach
    void setUp() {
        clock.setInstant(START);
        screeningId = testData.createScreening(START.plus(1, ChronoUnit.DAYS));
    }

    @Test
    void showsAllSeatsWithTheirCurrentStatus() {
        reservationService.reserve(screeningId, 3, 4, null);
        bookSeat(screeningId, 5, 6);

        SeatMapDto seatMap = seatMapService.getSeatMap(screeningId);

        assertThat(seatMap.rows()).hasSize(Auditorium.ROWS);
        assertThat(seatMap.rows()).allSatisfy(row ->
                assertThat(row.seats()).hasSize(Auditorium.SEATS_PER_ROW));

        assertThat(statusOf(seatMap, 3, 4)).isEqualTo(SeatAvailability.RESERVED);
        assertThat(statusOf(seatMap, 5, 6)).isEqualTo(SeatAvailability.BOOKED);
        assertThat(countOf(seatMap, SeatAvailability.AVAILABLE)).isEqualTo(198);
    }

    @Test
    void reservationIsShownAsAvailableOnceItExpires() {
        ReservationDto reservation = reservationService.reserve(screeningId, 3, 4, null);

        clock.setInstant(reservation.reservedUntil().minusSeconds(1));
        assertThat(statusOf(seatMapService.getSeatMap(screeningId), 3, 4))
                .isEqualTo(SeatAvailability.RESERVED);

        clock.setInstant(reservation.reservedUntil());
        assertThat(statusOf(seatMapService.getSeatMap(screeningId), 3, 4))
                .isEqualTo(SeatAvailability.AVAILABLE);
    }

    @Test
    void bookedSeatStaysBookedLongAfterTheReservationTime() {
        bookSeat(screeningId, 5, 6);

        clock.advance(Duration.ofHours(1));

        assertThat(statusOf(seatMapService.getSeatMap(screeningId), 5, 6))
                .isEqualTo(SeatAvailability.BOOKED);
    }

    @Test
    void showsOnlyReservationsOfTheRequestedScreening() {
        Long otherScreeningId = testData.createScreening(START.plus(2, ChronoUnit.DAYS));
        reservationService.reserve(otherScreeningId, 3, 4, null);
        bookSeat(otherScreeningId, 5, 6);

        SeatMapDto seatMap = seatMapService.getSeatMap(screeningId);

        assertThat(countOf(seatMap, SeatAvailability.AVAILABLE)).isEqualTo(200);
    }

    @Test
    void screeningThatHasStartedHasNoSeatMap() {
        // The screening from setUp starts one day after START.
        clock.setInstant(START.plus(1, ChronoUnit.DAYS));

        assertThatThrownBy(() -> seatMapService.getSeatMap(screeningId))
                .isInstanceOf(ScreeningNotAvailableException.class);
    }

    @Test
    void unknownScreeningHasNoSeatMap() {
        assertThatThrownBy(() -> seatMapService.getSeatMap(Long.MAX_VALUE))
                .isInstanceOf(ScreeningNotAvailableException.class);
    }

    private SeatAvailability statusOf(SeatMapDto seatMap, int seatRow, int seatNumber) {
        return seatMap.rows().stream()
                .filter(row -> row.seatRow() == seatRow)
                .flatMap(row -> row.seats().stream())
                .filter(seat -> seat.seatNumber() == seatNumber)
                .findFirst()
                .orElseThrow()
                .status();
    }

    private long countOf(SeatMapDto seatMap, SeatAvailability status) {
        return seatMap.rows().stream()
                .flatMap(row -> row.seats().stream())
                .filter(seat -> seat.status() == status)
                .count();
    }

    private void bookSeat(Long screeningId, int seatRow, int seatNumber) {
        ReservationDto reservation = reservationService.reserve(screeningId, seatRow, seatNumber, null);
        bookingService.book(new BookSeatRequest(reservation.token(), "Ada Lovelace",
                LocalDate.of(1990, 5, 17), "ada@example.com", null, true, true, false));
    }
}
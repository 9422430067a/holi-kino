package de.zahra.kino.reservation;

import de.zahra.kino.KinoServiceTest;
import de.zahra.kino.MutableClock;
import de.zahra.kino.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static de.zahra.kino.MutableClockConfiguration.START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@KinoServiceTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MutableClock clock;

    @Autowired
    private TestData testData;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long screeningId;

    @BeforeEach
    void setUp() {
        clock.setInstant(START);
        screeningId = testData.createScreening(START.plus(1, ChronoUnit.DAYS));
    }

    @Test
    void bookingTurnsTheReservationIntoABookedSeat() {
        ReservationDto reservation = reservationService.reserve(screeningId, 3, 4, null);
        clock.advance(Duration.ofMinutes(2));

        BookingDto result = bookingService.book(TestData.bookingRequest(reservation.token()));

        assertThat(result.seatRow()).isEqualTo(3);
        assertThat(result.seatNumber()).isEqualTo(4);

        // The seat row: booked, no deadline, points to the booking.
        Map<String, Object> seat = jdbcTemplate.queryForMap(
                "SELECT status, reserved_until, booking_id FROM seat_reservation WHERE reservation_token = ?",
                reservation.token());
        assertThat(seat.get("status")).isEqualTo("BOOKED");
        assertThat(seat.get("reserved_until")).isNull();
        assertThat(seat.get("booking_id")).isEqualTo(result.bookingId());

        // The booking row: consents stored at the moment of booking, newsletter not accepted.
        Booking booking = bookingRepository.findById(result.bookingId()).orElseThrow();
        assertThat(booking.getTermsAcceptedAt()).isEqualTo(clock.instant());
        assertThat(booking.getPrivacyAcceptedAt()).isEqualTo(clock.instant());
        assertThat(booking.getNewsletterAcceptedAt()).isNull();
    }

    @Test
    void expiredReservationCannotBeBookedAndLeavesNoBooking() {
        ReservationDto reservation = reservationService.reserve(screeningId, 3, 4, null);
        clock.setInstant(reservation.reservedUntil());
        long bookingsBefore = bookingRepository.count();

        assertThatThrownBy(() -> bookingService.book(TestData.bookingRequest(reservation.token())))
                .isInstanceOf(ReservationNotActiveException.class);

        assertThat(bookingRepository.count()).isEqualTo(bookingsBefore);
    }

    @Test
    void reservationCanBeBookedOnlyOnce() {
        ReservationDto reservation = reservationService.reserve(screeningId, 3, 4, null);
        bookingService.book(TestData.bookingRequest(reservation.token()));
        long bookingsAfterFirst = bookingRepository.count();

        assertThatThrownBy(() -> bookingService.book(TestData.bookingRequest(reservation.token())))
                .isInstanceOf(ReservationNotActiveException.class);

        assertThat(bookingRepository.count()).isEqualTo(bookingsAfterFirst);
    }
}
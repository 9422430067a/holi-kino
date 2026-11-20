package de.zahra.kino.reservation;

import de.zahra.kino.KinoServiceTest;
import de.zahra.kino.MutableClock;
import de.zahra.kino.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static de.zahra.kino.MutableClockConfiguration.START;
import static org.assertj.core.api.Assertions.assertThat;

@KinoServiceTest
class ReservationCleanupServiceTest {

    @Autowired
    private ReservationCleanupService cleanupService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MutableClock clock;

    @Autowired
    private TestData testData;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext context;

    private Long screeningId;

    @BeforeEach
    void setUp() {
        clock.setInstant(START);
        screeningId = testData.createScreening(START.plus(1, ChronoUnit.DAYS));
    }

    @Test
    void deletesOnlyExpiredReservations() {
        ReservationDto expired = reservationService.reserve(screeningId, 3, 4, null);

        clock.advance(Duration.ofMinutes(4));
        ReservationDto stillActive = reservationService.reserve(screeningId, 3, 5, null);
        ReservationDto booked = reservationService.reserve(screeningId, 3, 6, null);
        bookingService.book(TestData.bookingRequest(booked.reservationToken()));

        // Exactly at the deadline of the first reservation.
        clock.setInstant(expired.reservedUntil());
        int deleted = cleanupService.deleteExpiredReservations();

        assertThat(deleted).isGreaterThanOrEqualTo(1);
        assertThat(rowExists(expired.reservationToken())).isFalse();
        assertThat(rowExists(stillActive.reservationToken())).isTrue();
        assertThat(rowExists(booked.reservationToken())).isTrue();
    }

    @Test
    void schedulerIsDisabledInTests() {
        assertThat(context.getBeansOfType(ReservationCleanupScheduler.class)).isEmpty();
    }

    private boolean rowExists(UUID token) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM seat_reservation WHERE reservation_token = ?", Integer.class, token);
        return count != null && count > 0;
    }
}
package de.zahra.kino.reservation;

import de.zahra.kino.MutableClock;
import de.zahra.kino.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReservationServiceTest {

    private static final Instant START = Instant.parse("2036-01-01T12:00:00Z");

    @TestConfiguration
    static class MutableClockConfiguration {

        @Bean
        @Primary
        MutableClock mutableClock() {
            return new MutableClock(START);
        }
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long screeningId;

    @BeforeEach
    void setUp() {
        clock.setInstant(START);
        screeningId = createScreening(START.plus(1, ChronoUnit.DAYS));
    }

    @Test
    void expiredReservationCanBeTakenByAnotherGuest() {
        ReservationDto guestA = reservationService.reserve(screeningId, 3, 4, null);

        // Shortly before expiry the seat still belongs to guest A.
        clock.advance(Duration.ofMinutes(4).plusSeconds(59));
        assertThatThrownBy(() -> reservationService.reserve(screeningId, 3, 4, null))
                .isInstanceOf(SeatAlreadyTakenException.class);

        // Shortly after expiry guest B can take it over.
        clock.advance(Duration.ofSeconds(2));
        ReservationDto guestB = reservationService.reserve(screeningId, 3, 4, null);

        assertThat(guestB.token()).isNotEqualTo(guestA.token());
        assertThat(guestB.reservedUntil()).isEqualTo(clock.instant().plus(Duration.ofMinutes(5)));
        assertThat(tokenInDatabase(3, 4)).isEqualTo(guestB.token());
    }

    private Long createScreening(Instant startsAt) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO screening (movie_id, starts_at) "
                        + "VALUES ((SELECT id FROM movie ORDER BY id LIMIT 1), ?) RETURNING id",
                Long.class, OffsetDateTime.ofInstant(startsAt, ZoneOffset.UTC));
    }

    private UUID tokenInDatabase(int seatRow, int seatNumber) {
        return jdbcTemplate.queryForObject(
                "SELECT reservation_token FROM seat_reservation "
                        + "WHERE screening_id = ? AND seat_row = ? AND seat_number = ?",
                UUID.class, screeningId, seatRow, seatNumber);
    }

    @Test
    void seatChangeKeepsTokenAndDeadlineAndReleasesOldSeat() {
        ReservationDto first = reservationService.reserve(screeningId, 3, 4, null);

        // Change seats two minutes later, so a recalculated deadline would be noticed.
        clock.advance(Duration.ofMinutes(2));
        ReservationDto changed = reservationService.reserve(screeningId, 3, 5, first.token());

        assertThat(changed.token()).isEqualTo(first.token());
        assertThat(changed.reservedUntil()).isEqualTo(first.reservedUntil());
        assertThat(changed.seatNumber()).isEqualTo(5);

        // The old seat is free again: another guest can reserve it right away.
        ReservationDto otherGuest = reservationService.reserve(screeningId, 3, 4, null);
        assertThat(otherGuest.token()).isNotEqualTo(first.token());

        // The guest holds exactly one seat, not two.
        assertThat(rowsWithToken(first.token())).isEqualTo(1);
    }

    private int rowsWithToken(UUID token) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM seat_reservation WHERE reservation_token = ?",
                Integer.class, token);
    }

    @Test
    void failedSeatChangeKeepsTheOldSeat() {
        ReservationDto otherGuest = reservationService.reserve(screeningId, 3, 5, null);
        ReservationDto mine = reservationService.reserve(screeningId, 3, 4, null);

        assertThatThrownBy(() -> reservationService.reserve(screeningId, 3, 5, mine.token()))
                .isInstanceOf(SeatAlreadyTakenException.class);

        // Both guests still hold exactly the seats they had before.
        assertThat(tokenInDatabase(3, 4)).isEqualTo(mine.token());
        assertThat(tokenInDatabase(3, 5)).isEqualTo(otherGuest.token());
        assertThat(rowsWithToken(mine.token())).isEqualTo(1);
    }
}
package de.zahra.kino.reservation;

import de.zahra.kino.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReservationConcurrencyTest {

    private static final Instant NOW = Instant.parse("2036-01-01T12:00:00Z");

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void onlyOneOfManyConcurrentGuestsGetsTheSeat() throws Exception {
        Long screeningId = createScreening(NOW.plus(1, ChronoUnit.DAYS));
        int guests = 20;

        ExecutorService executor = Executors.newFixedThreadPool(guests);
        CountDownLatch allReady = new CountDownLatch(guests);
        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < guests; i++) {
            results.add(executor.submit(() -> {
                allReady.countDown();
                startSignal.await();
                try {
                    reservationService.reserve(screeningId, 5, 7, null);
                    return true;
                } catch (SeatAlreadyTakenException e) {
                    return false;
                }
            }));
        }

        allReady.await();  // The main thread waits until all threads are ready
        startSignal.countDown();

        int successes = 0;
        for (Future<Boolean> result : results) {
            if (result.get(30, TimeUnit.SECONDS)) {
                successes++;
            }
        }
        executor.shutdown();

        assertThat(successes).isEqualTo(1);

        Integer rows = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM seat_reservation "
                        + "WHERE screening_id = ? AND seat_row = 5 AND seat_number = 7",
                Integer.class, screeningId);
        assertThat(rows).isEqualTo(1);
    }

    private Long createScreening(Instant startsAt) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO screening (movie_id, starts_at) "
                        + "VALUES ((SELECT id FROM movie ORDER BY id LIMIT 1), ?) RETURNING id",
                Long.class, OffsetDateTime.ofInstant(startsAt, ZoneOffset.UTC));
    }
}
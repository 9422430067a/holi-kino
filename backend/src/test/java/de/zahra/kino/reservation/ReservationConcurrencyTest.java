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
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static de.zahra.kino.MutableClockConfiguration.START;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestcontainersConfiguration.class, MutableClockConfiguration.class, TestData.class})
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MutableClock clock;

    @Autowired
    private TestData testData;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // The clock is shared with other test classes, so reset it.
        clock.setInstant(START);
    }

    @Test
    void onlyOneOfManyConcurrentGuestsGetsTheSeat() throws Exception {
        Long screeningId = testData.createScreening(START.plus(1, ChronoUnit.DAYS));
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

    @Test
    void concurrentSeatChangesOfOneGuestLeaveExactlyOneSeat() throws Exception {
        Long screeningId = testData.createScreening(START.plus(1, ChronoUnit.DAYS));
        ReservationDto original = reservationService.reserve(screeningId, 3, 4, null);
        int changes = 10;

        ExecutorService pool = Executors.newFixedThreadPool(changes);
        CountDownLatch allReady = new CountDownLatch(changes);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < changes; i++) {
            int seatNumber = 5 + i; // each change goes to a different seat: 5 to 14
            results.add(pool.submit(() -> {
                allReady.countDown();
                startSignal.await();
                try {
                    reservationService.reserve(screeningId, 3, seatNumber, original.token());
                    return true;
                } catch (ReservationNotActiveException e) {
                    return false;
                }
            }));
        }

        allReady.await();
        startSignal.countDown();

        int successes = 0;
        for (Future<Boolean> result : results) {
            if (result.get(10, TimeUnit.SECONDS)) {
                successes++;
            }
        }
        pool.shutdown();

        assertThat(successes).isGreaterThanOrEqualTo(1);
        assertThat(countRows("SELECT count(*) FROM seat_reservation WHERE reservation_token = ?",
                original.token())).isEqualTo(1);
        assertThat(countRows("SELECT count(*) FROM seat_reservation WHERE screening_id = ?",
                screeningId)).isEqualTo(1);
    }

    private int countRows(String sql, Object parameter) {
        return jdbcTemplate.queryForObject(sql, Integer.class, parameter);
    }
}
package de.zahra.kino.screening;

import de.zahra.kino.KinoServiceTest;
import de.zahra.kino.MutableClock;
import de.zahra.kino.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.temporal.ChronoUnit;

import static de.zahra.kino.MutableClockConfiguration.START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@KinoServiceTest
class ScreeningServiceTest {

    @Autowired
    private ScreeningService screeningService;

    @Autowired
    private MutableClock clock;

    @Autowired
    private TestData testData;

    @BeforeEach
    void setUp() {
        clock.setInstant(START);
    }

    @Test
    void returnsAScreeningThatHasAlreadyStarted() {
        Long screeningId = testData.createScreening(START.minus(1, ChronoUnit.HOURS));

        ScreeningDto screening = screeningService.getScreening(screeningId);

        assertThat(screening.id()).isEqualTo(screeningId);
        assertThat(screening.movieTitle()).isNotBlank();
        assertThat(screening.startsAt()).isEqualTo(START.minus(1, ChronoUnit.HOURS));
    }

    @Test
    void unknownScreeningIsNotFound() {
        assertThatThrownBy(() -> screeningService.getScreening(Long.MAX_VALUE))
                .isInstanceOf(ScreeningNotFoundException.class);
    }
}
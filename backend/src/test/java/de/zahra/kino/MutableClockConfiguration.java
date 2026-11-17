package de.zahra.kino;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;

/**
 * Replaces the real clock with a MutableClock. Tests reset it to START before each test.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MutableClockConfiguration {

    public static final Instant START = Instant.parse("2036-01-01T12:00:00Z");

    @Bean
    @Primary
    MutableClock mutableClock() {
        return new MutableClock(START);
    }
}
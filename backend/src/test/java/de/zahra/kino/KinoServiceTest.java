package de.zahra.kino;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Shared setup for service tests: real Postgres in a container, a mutable clock,
 * test data helpers, and no background cleanup job. One identical configuration
 * lets Spring reuse a single context and container for all of these tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(properties = "kino.reservation.cleanup.enabled=false")
@Import({TestcontainersConfiguration.class, MutableClockConfiguration.class, TestData.class})
public @interface KinoServiceTest {
}
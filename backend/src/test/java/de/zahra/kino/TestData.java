package de.zahra.kino;

import de.zahra.kino.reservation.BookSeatRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Creates test data directly in the database, so tests do not depend on the seed data.
 */
public class TestData {

    private final JdbcTemplate jdbcTemplate;

    public TestData(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createScreening(Instant startsAt) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO screening (movie_id, starts_at) "
                        + "VALUES ((SELECT id FROM movie ORDER BY id LIMIT 1), ?) RETURNING id",
                Long.class, OffsetDateTime.ofInstant(startsAt, ZoneOffset.UTC));
    }

    public static BookSeatRequest bookingRequest(UUID token) {
        return new BookSeatRequest(token, "Ada Lovelace", LocalDate.of(1990, 5, 17),
                "ada@example.com", null, true, true, false);
    }
}
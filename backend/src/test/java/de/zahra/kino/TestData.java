package de.zahra.kino;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
}
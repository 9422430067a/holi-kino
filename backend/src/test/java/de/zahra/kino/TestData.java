package de.zahra.kino;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
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

    public void createBookedSeat(Long screeningId, int seatRow, int seatNumber) {
        UUID bookingId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO booking (id, full_name, birth_date, email, terms_accepted_at) "
                        + "VALUES (?, 'Test Guest', DATE '1990-01-01', 'guest@example.com', now())",
                bookingId);
        jdbcTemplate.update(
                "INSERT INTO seat_reservation "
                        + "(screening_id, seat_row, seat_number, status, reservation_token, reserved_until, booking_id) "
                        + "VALUES (?, ?, ?, 'BOOKED', ?, NULL, ?)",
                screeningId, seatRow, seatNumber, UUID.randomUUID(), bookingId);
    }
}
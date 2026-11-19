package de.zahra.kino.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatReservationRepository reservationRepository;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository,
                          SeatReservationRepository reservationRepository,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public BookingDto book(BookSeatRequest request) {
        Instant now = Instant.now(clock);

        // The booking row must exist before the reservation points to it (foreign key),
        // so it is flushed right away instead of at commit.
        Booking booking = bookingRepository.saveAndFlush(new Booking(
                request.fullName().trim(),
                request.birthDate(),
                request.email().trim(),
                blankToNull(request.phone()),
                // Required consents: if validation was bypassed, the NOT NULL columns reject null.
                Boolean.TRUE.equals(request.termsAccepted()) ? now : null,
                Boolean.TRUE.equals(request.privacyAccepted()) ? now : null,
                Boolean.TRUE.equals(request.newsletterAccepted()) ? now : null,
                now));

        int bookedRows = reservationRepository.book(request.reservationToken(), booking.getId(), now);
        if (bookedRows == 0) {
            // Expired, unknown or already booked: the exception rolls back the booking row too.
            throw new ReservationNotActiveException(request.reservationToken());
        }

        SeatReservation seat = reservationRepository.findByBookingId(booking.getId()).orElseThrow();
        return new BookingDto(booking.getId(), seat.getScreeningId(), seat.getSeatRow(), seat.getSeatNumber());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
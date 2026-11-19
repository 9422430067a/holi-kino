package de.zahra.kino.reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ReservationCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ReservationCleanupService.class);

    private final SeatReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationCleanupService(SeatReservationRepository reservationRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    /**
     * Removes expired reservations. Only keeps the table small:
     * every read already treats expired reservations as free.
     */
    @Transactional
    public int deleteExpiredReservations() {
        int deleted = reservationRepository.deleteExpired(Instant.now(clock));
        if (deleted > 0) {
            log.info("Deleted {} expired reservations", deleted);
        }
        return deleted;
    }
}
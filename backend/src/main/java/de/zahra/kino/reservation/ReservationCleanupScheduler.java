package de.zahra.kino.reservation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kino.reservation.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class ReservationCleanupScheduler {

    private final ReservationCleanupService cleanupService;

    public ReservationCleanupScheduler(ReservationCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(fixedDelayString = "${kino.reservation.cleanup.interval:PT1M}")
    public void cleanUp() {
        cleanupService.deleteExpiredReservations();
    }
}
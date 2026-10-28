package com.alertamujer.alerts.adapter.in.scheduler;

import com.alertamujer.alerts.application.port.in.ReviewTripsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Input adapter: every minute reviews the "Avísame si no llego" trips. Starts 10 s after
 * startup: if the server was asleep (free Render), the overdue ones are alerted as soon as it wakes up.
 */
@Component
class TripsScheduler {

    private final ReviewTripsUseCase review;

    TripsScheduler(ReviewTripsUseCase review) {
        this.review = review;
    }

    @Scheduled(fixedDelayString = "${trips.check-ms:60000}", initialDelay = 10000)
    void review() {
        review.review(LocalDateTime.now());
    }
}

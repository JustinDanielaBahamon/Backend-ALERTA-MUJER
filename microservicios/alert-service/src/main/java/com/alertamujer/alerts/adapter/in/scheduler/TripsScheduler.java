package com.alertamujer.alerts.adapter.in.scheduler;

import com.alertamujer.alerts.application.port.in.ReviewTripsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adaptador de entrada: cada minuto revisa los trayectos de "Avísame si no llego". Empieza a los 10 s
 * de arrancar: si el servidor estaba dormido (Render gratis), los vencidos se alertan apenas despierte.
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

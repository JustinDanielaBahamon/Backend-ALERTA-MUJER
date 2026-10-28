package com.alertamujer.alerts.adapter.in.scheduler;

import com.alertamujer.alerts.application.port.in.CleanUpLocationsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Input adapter: cleans the location history once a day. */
@Component
class LocationCleanupScheduler {

    private final CleanUpLocationsUseCase clear;

    LocationCleanupScheduler(CleanUpLocationsUseCase clear) {
        this.clear = clear;
    }

    // Every day at 3:30 a.m. (Colombia time), when almost nobody uses the app
    @Scheduled(cron = "${locations.retention.cron:0 30 3 * * *}", zone = "America/Bogota")
    void clear() {
        clear.clear(LocalDateTime.now());
    }
}

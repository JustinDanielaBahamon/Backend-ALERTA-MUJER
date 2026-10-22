package com.alertamujer.evidence.adapter.in.scheduler;

import com.alertamujer.evidence.application.port.in.CleanUpEvidenceUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Time-based input adapter: triggers the evidence cleanup every night. */
@Component
class EvidenceCleanupScheduler {

    private final CleanUpEvidenceUseCase clear;

    EvidenceCleanupScheduler(CleanUpEvidenceUseCase clear) {
        this.clear = clear;
    }

    // Every day at 4:00 a.m. (Colombia time)
    @Scheduled(cron = "${evidence.retention.cron:0 0 4 * * *}", zone = "America/Bogota")
    void clear() {
        clear.clear(LocalDateTime.now());
    }
}

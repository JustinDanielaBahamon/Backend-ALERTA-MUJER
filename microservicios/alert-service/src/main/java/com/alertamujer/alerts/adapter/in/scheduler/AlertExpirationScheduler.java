package com.alertamujer.alerts.adapter.in.scheduler;

import com.alertamujer.alerts.application.port.in.ExpireAlertsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Adaptador de entrada: cada minuto revisa las alertas que superaron la duración máxima. */
@Component
class AlertExpirationScheduler {

    private final ExpireAlertsUseCase expire;

    AlertExpirationScheduler(ExpireAlertsUseCase expire) {
        this.expire = expire;
    }

    @Scheduled(fixedDelayString = "${alerts.expiration.check-ms:60000}", initialDelay = 30000)
    void review() {
        expire.review(LocalDateTime.now());
    }
}

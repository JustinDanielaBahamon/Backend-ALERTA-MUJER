package com.alertamujer.alerts.adapter.in.scheduler;

import com.alertamujer.alerts.application.port.in.ExpirarAlertasUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Adaptador de entrada: cada minuto revisa las alertas que superaron la duración máxima. */
@Component
class ExpiracionAlertasScheduler {

    private final ExpirarAlertasUseCase expirar;

    ExpiracionAlertasScheduler(ExpirarAlertasUseCase expirar) {
        this.expirar = expirar;
    }

    @Scheduled(fixedDelayString = "${alerts.expiration.check-ms:60000}", initialDelay = 30000)
    void revisar() {
        expirar.revisar(LocalDateTime.now());
    }
}

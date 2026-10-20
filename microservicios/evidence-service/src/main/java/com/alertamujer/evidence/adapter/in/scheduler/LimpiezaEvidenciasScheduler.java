package com.alertamujer.evidence.adapter.in.scheduler;

import com.alertamujer.evidence.application.port.in.LimpiarEvidenciasUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Adaptador de entrada por tiempo: dispara la limpieza de evidencias todas las noches. */
@Component
class LimpiezaEvidenciasScheduler {

    private final LimpiarEvidenciasUseCase limpiar;

    LimpiezaEvidenciasScheduler(LimpiarEvidenciasUseCase limpiar) {
        this.limpiar = limpiar;
    }

    // Todos los días a las 4:00 a. m. (hora de Colombia)
    @Scheduled(cron = "${evidence.retention.cron:0 0 4 * * *}", zone = "America/Bogota")
    void limpiar() {
        limpiar.limpiar(LocalDateTime.now());
    }
}

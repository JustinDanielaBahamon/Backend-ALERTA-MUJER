package com.alertamujer.alerts.adapter.in.scheduler;

import com.alertamujer.alerts.application.port.in.LimpiarUbicacionesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Adaptador de entrada: limpia el historial de ubicaciones una vez al día. */
@Component
class LimpiezaUbicacionesScheduler {

    private final LimpiarUbicacionesUseCase clear;

    LimpiezaUbicacionesScheduler(LimpiarUbicacionesUseCase clear) {
        this.clear = clear;
    }

    // Todos los días a las 3:30 a. m. (hora de Colombia), cuando casi nadie usa la app
    @Scheduled(cron = "${locations.retention.cron:0 30 3 * * *}", zone = "America/Bogota")
    void clear() {
        clear.clear(LocalDateTime.now());
    }
}

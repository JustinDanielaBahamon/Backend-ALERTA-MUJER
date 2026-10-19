package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.infrastructure.repository.LocationLogRepository;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Limpia el historial de ubicaciones: la app envía la ubicación cada pocos minutos y sin límite la
 * tabla crecería con cada usuaria. Lo del día a día se guarda poco; los recorridos de una alerta,
 * más tiempo (pueden servir como prueba). La usuaria ve estos plazos en la app y en la web.
 */
@Component
public class LocationRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(LocationRetentionJob.class);

    private final LocationLogRepository repository;
    private final int diasSinAlerta;
    private final int diasDeAlertas;

    public LocationRetentionJob(LocationLogRepository repository,
                                @Value("${locations.retention.days-without-alert:7}") int diasSinAlerta,
                                @Value("${locations.retention.days-with-alert:30}") int diasDeAlertas) {
        this.repository = repository;
        this.diasSinAlerta = diasSinAlerta;
        this.diasDeAlertas = diasDeAlertas;
    }

    // Todos los días a las 3:30 a. m. (hora de Colombia), cuando casi nadie usa la app
    @Scheduled(cron = "${locations.retention.cron:0 30 3 * * *}", zone = "America/Bogota")
    public void limpiar() {
        limpiar(LocalDateTime.now());
    }

    int limpiar(LocalDateTime ahora) {
        int borradas = AuditSource.con("Tarea automática · Limpieza del historial de ubicaciones", () ->
                repository.borrarSinAlertaAntesDe(ahora.minusDays(diasSinAlerta))
                        + repository.borrarDeAlertasAntesDe(ahora.minusDays(diasDeAlertas)));
        if (borradas > 0) log.info("Ubicaciones antiguas eliminadas: {}", borradas);
        return borradas;
    }
}

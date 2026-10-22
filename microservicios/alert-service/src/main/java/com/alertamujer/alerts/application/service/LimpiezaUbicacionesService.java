package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.LimpiarUbicacionesUseCase;
import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** Borra lo del día a día a los 7 días y los recorridos de las alertas a los 30 (configurable). */
@Service
public class LimpiezaUbicacionesService implements LimpiarUbicacionesUseCase {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaUbicacionesService.class);

    private final UbicacionRepositorio repository;
    private final int diasSinAlerta;
    private final int diasDeAlertas;

    public LimpiezaUbicacionesService(UbicacionRepositorio repository,
                                      @Value("${locations.retention.days-without-alert:7}") int diasSinAlerta,
                                      @Value("${locations.retention.days-with-alert:30}") int diasDeAlertas) {
        this.repository = repository;
        this.diasSinAlerta = diasSinAlerta;
        this.diasDeAlertas = diasDeAlertas;
    }

    @Override
    public int clear(LocalDateTime now) {
        int borradas = AuditSource.runAs("Tarea automática · Limpieza del historial de ubicaciones", () ->
                repository.borrarSinAlertaAntesDe(now.minusDays(diasSinAlerta))
                        + repository.borrarDeAlertasAntesDe(now.minusDays(diasDeAlertas)));
        if (borradas > 0) log.info("Ubicaciones antiguas eliminadas: {}", borradas);
        return borradas;
    }
}

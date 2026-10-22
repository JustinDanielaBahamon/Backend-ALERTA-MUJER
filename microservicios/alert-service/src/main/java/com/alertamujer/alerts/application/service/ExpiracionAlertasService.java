package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ExpirarAlertasUseCase;
import com.alertamujer.alerts.application.port.out.AjustesPort;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Expira las alertas que superan la duración máxima configurada por la administración. */
@Service
public class ExpiracionAlertasService implements ExpirarAlertasUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpiracionAlertasService.class);

    private final AlertaRepositorio repository;
    private final AjustesPort ajustes;
    private final EventosAlertaPort events;

    // Última ejecución, para el estado del sistema en el panel
    private volatile LocalDateTime ultimaRevision;
    private volatile int expiradasUltimaRevision;

    public ExpiracionAlertasService(AlertaRepositorio repository, AjustesPort ajustes, EventosAlertaPort events) {
        this.repository = repository;
        this.ajustes = ajustes;
        this.events = events;
    }

    @Override
    public int revisar(LocalDateTime ahora) {
        // guardarTodas abre la transacción dentro de AuditSource.con, así la auditoría registra el origen
        int expiradas = AuditSource.con("Tarea automática · Expiración de alertas", () -> expirarVencidas(ahora));
        ultimaRevision = ahora;
        expiradasUltimaRevision = expiradas;
        if (expiradas > 0) log.info("Alertas expiradas por superar la duración máxima: {}", expiradas);
        return expiradas;
    }

    @Override
    public LocalDateTime ultimaRevision() {
        return ultimaRevision;
    }

    @Override
    public int expiradasUltimaRevision() {
        return expiradasUltimaRevision;
    }

    int expirarVencidas(LocalDateTime ahora) {
        int minutos = ajustes.duracionMaximaAlertaMinutos();
        if (minutos <= 0) return 0;
        LocalDateTime limite = ahora.minusMinutes(minutos);
        List<Alert> vencidas = repository.activas().stream().filter(a -> a.vencida(limite)).toList();
        vencidas.forEach(a -> a.expirar(ahora));
        repository.guardarTodas(vencidas);
        vencidas.forEach(a -> events.alertaTerminada(a, ahora));
        return vencidas.size();
    }
}

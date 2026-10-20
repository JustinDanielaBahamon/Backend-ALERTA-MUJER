package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.AlertasRecibidasUseCase;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase;
import com.alertamujer.notifications.application.port.out.AlertasPort;
import com.alertamujer.notifications.application.port.out.IdentidadPort;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Alertas de las personas que tienen a la usuaria como contacto de emergencia. */
@Service
public class AlertasRecibidasService implements AlertasRecibidasUseCase {

    static final Duration RECIENTES = Duration.ofDays(30);

    private final AlertasPort alertas;
    private final IdentidadPort identidad;
    private final SeguimientoPublicoUseCase seguimiento;

    public AlertasRecibidasService(AlertasPort alertas, IdentidadPort identidad, SeguimientoPublicoUseCase seguimiento) {
        this.alertas = alertas;
        this.identidad = identidad;
        this.seguimiento = seguimiento;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceivedAlert> recibidas(Long userId, LocalDateTime ahora) {
        String correo = identidad.correoDe(userId).orElse(null);
        Long miPerfil = identidad.perfilDe(userId).orElse(null);
        return alertas.buscarVarias(alertas.avisadasA(correo)).stream()
                .filter(a -> !Objects.equals(a.userProfileId(), miPerfil)) // las propias no son "recibidas"
                .filter(a -> a.startedAt() != null && a.startedAt().isAfter(ahora.minus(RECIENTES)))
                .sorted(Comparator.comparing(AlertSummary::startedAt).reversed())
                .map(a -> vista(a, ahora))
                .toList();
    }

    @Override
    @Transactional
    public String enlaceSeguimiento(Long userId, Long alertId, LocalDateTime ahora) {
        ReceivedAlert alerta = recibidas(userId, ahora).stream()
                .filter(a -> a.alertId().equals(alertId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("La alerta no existe"));
        if (!alerta.trackingAvailable()) {
            throw new ResourceNotFoundException("El seguimiento de esta alerta ya terminó");
        }
        return seguimiento.crearEnlace(alertId);
    }

    private ReceivedAlert vista(AlertSummary a, LocalDateTime ahora) {
        String nombre = identidad.nombreDePilaDelPerfil(a.userProfileId()).orElse("");
        LocalDateTime fin = SeguimientoPublicoService.fin(a);
        boolean activa = "active".equals(a.status());
        // Aquí una alerta terminada sin hora de cierre ya no ofrece seguimiento
        boolean disponible = activa || (fin != null && TrackingLink.seguimientoVigente(a.status(), fin, ahora));
        return new ReceivedAlert(a.id(), nombre, a.status(), activa, a.startedAt(), fin, disponible);
    }
}

package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.VistasAlertaUseCase;
import com.alertamujer.notifications.application.port.out.AlertasPort;
import com.alertamujer.notifications.application.port.out.IdentidadPort;
import com.alertamujer.notifications.application.port.out.VistasRepositorio;
import com.alertamujer.notifications.application.port.out.VistasRepositorio.Conteo;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** Vistas del seguimiento: solo la dueña de la alerta puede consultarlas. */
@Service
public class VistasAlertaService implements VistasAlertaUseCase {

    /** El seguimiento se actualiza cada pocos segundos: quien pasó hace menos de esto lo está viendo. */
    static final Duration VIENDO_AHORA = Duration.ofSeconds(90);

    private final AlertasPort alertas;
    private final IdentidadPort identidad;
    private final VistasRepositorio vistas;

    public VistasAlertaService(AlertasPort alertas, IdentidadPort identidad, VistasRepositorio vistas) {
        this.alertas = alertas;
        this.identidad = identidad;
        this.vistas = vistas;
    }

    @Override
    public Conteo vistas(Long alertId, Long userId, LocalDateTime ahora) {
        AlertSummary alerta = alertas.buscar(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        Long perfil = identidad.perfilDe(userId).orElse(null);
        // Una alerta ajena se responde igual que una inexistente: no se revela que existe
        if (!Objects.equals(perfil, alerta.userProfileId())) throw new ResourceNotFoundException("Alert", alertId);
        return vistas.contar(alertId, ahora.minus(VIENDO_AHORA));
    }
}

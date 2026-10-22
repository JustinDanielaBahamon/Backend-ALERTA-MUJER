package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.VistasAlertaUseCase;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
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

    private final AlertsPort alerts;
    private final IdentityPort identity;
    private final VistasRepositorio vistas;

    public VistasAlertaService(AlertsPort alerts, IdentityPort identity, VistasRepositorio vistas) {
        this.alerts = alerts;
        this.identity = identity;
        this.vistas = vistas;
    }

    @Override
    public Conteo vistas(Long alertId, Long userId, LocalDateTime now) {
        AlertSummary alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        Long profile = identity.profileOf(userId).orElse(null);
        // Una alerta ajena se responde igual que una inexistente: no se revela que existe
        if (!Objects.equals(profile, alert.userProfileId())) throw new ResourceNotFoundException("Alert", alertId);
        return vistas.count(alertId, now.minus(VIENDO_AHORA));
    }
}

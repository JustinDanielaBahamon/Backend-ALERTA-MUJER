package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase;
import com.alertamujer.notifications.application.port.out.AjustesPort;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.EnlaceRepositorio;
import com.alertamujer.notifications.application.port.out.EvidenciasPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.application.port.out.VistasRepositorio;
import com.alertamujer.notifications.domain.model.LimiteMensajesContacto;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Enlaces de seguimiento y lo que ve el contacto con ellos: ubicación, grabaciones y chat. */
@Service
public class SeguimientoPublicoService implements SeguimientoPublicoUseCase {

    private final EnlaceRepositorio enlaces;
    private final AlertsPort alerts;
    private final IdentityPort identity;
    private final AjustesPort ajustes;
    private final EvidenciasPort evidences;
    private final VistasRepositorio vistas;
    private final LimiteMensajesContacto limite = new LimiteMensajesContacto();

    public SeguimientoPublicoService(EnlaceRepositorio enlaces, AlertsPort alerts, IdentityPort identity,
                                     AjustesPort ajustes, EvidenciasPort evidences, VistasRepositorio vistas) {
        this.enlaces = enlaces;
        this.alerts = alerts;
        this.identity = identity;
        this.ajustes = ajustes;
        this.evidences = evidences;
        this.vistas = vistas;
    }

    @Override
    @Transactional
    public String crearEnlace(Long alertId) {
        String code = TrackingLink.generarCodigo();
        enlaces.save(TrackingLink.para(alertId, code));
        return code;
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingView consultar(String code, LocalDateTime now) {
        return view(alertaVigente(code, now));
    }

    @Override
    @Transactional
    public TrackingView consultar(String code, String visitante, LocalDateTime now) {
        AlertSummary alert = alertaVigente(code, now);
        if (visitante != null && visitante.matches("[A-Za-z0-9_-]{8,64}")) {
            vistas.registrar(alert.id(), visitante, now);
        }
        return view(alert);
    }

    private TrackingView view(AlertSummary alert) {
        String name = identity.nombreDePilaDelPerfil(alert.userProfileId()).orElse("");
        List<LocationPoint> recorrido = new ArrayList<>(alerts.recorrido(alert.id()));
        if (recorrido.isEmpty() && alert.latitude() != null && alert.longitude() != null) {
            recorrido.add(new LocationPoint(alert.latitude(), alert.longitude(), null, alert.startedAt()));
        }
        // Video y audio grabados durante la alerta: el contacto puede verlos para ayudar a ubicarla
        List<EvidenciaPublica> grabaciones = evidences.deAlerta(alert.id()).stream()
                .map(e -> new EvidenciaPublica(e.id(), e.mediaType(), e.contentType(), e.createdAt()))
                .toList();
        return new TrackingView(name, alert.status(), "active".equals(alert.status()), alert.startedAt(),
                fin(alert), recorrido, ajustes.emergencyNumber(), grabaciones);
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceFile evidence(String code, Long evidenciaId, LocalDateTime now) {
        AlertSummary alert = alertaVigente(code, now);
        return evidences.file(alert.id(), evidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("La evidencia no existe"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertMessageView> mensajes(String code, Long afterId, LocalDateTime now) {
        return alerts.mensajes(alertaVigente(code, now).id(), afterId);
    }

    @Override
    @Transactional
    public AlertMessageView enviarMensaje(String code, String name, String text, LocalDateTime now) {
        AlertSummary alert = alertaVigente(code, now);
        limite.registrar(TrackingLink.hash(code), now);
        return alerts.mensajeDeContacto(alert.id(), name, text);
    }

    private AlertSummary alertaVigente(String code, LocalDateTime now) {
        if (!TrackingLink.formatoPosible(code)) throw noDisponible();
        TrackingLink enlace = enlaces.porHash(TrackingLink.hash(code)).orElseThrow(SeguimientoPublicoService::noDisponible);
        if (enlace.revocado()) throw noDisponible();
        AlertSummary alert = alerts.find(enlace.getAlertId()).orElseThrow(SeguimientoPublicoService::noDisponible);
        if (!TrackingLink.seguimientoVigente(alert.status(), fin(alert), now)) throw noDisponible();
        return alert;
    }

    static LocalDateTime fin(AlertSummary a) {
        return a.endedAt() != null ? a.endedAt() : a.cancelledAt();
    }

    private static ResourceNotFoundException noDisponible() {
        return new ResourceNotFoundException("El enlace de seguimiento no existe o ya venció");
    }
}

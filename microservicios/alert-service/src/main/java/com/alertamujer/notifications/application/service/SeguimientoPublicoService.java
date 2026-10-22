package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.notifications.application.port.in.SeguimientoPublicoUseCase;
import com.alertamujer.notifications.application.port.out.AjustesPort;
import com.alertamujer.notifications.application.port.out.AlertasPort;
import com.alertamujer.notifications.application.port.out.EnlaceRepositorio;
import com.alertamujer.notifications.application.port.out.EvidenciasPort;
import com.alertamujer.notifications.application.port.out.IdentidadPort;
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
    private final AlertasPort alertas;
    private final IdentidadPort identidad;
    private final AjustesPort ajustes;
    private final EvidenciasPort evidencias;
    private final VistasRepositorio vistas;
    private final LimiteMensajesContacto limite = new LimiteMensajesContacto();

    public SeguimientoPublicoService(EnlaceRepositorio enlaces, AlertasPort alertas, IdentidadPort identidad,
                                     AjustesPort ajustes, EvidenciasPort evidencias, VistasRepositorio vistas) {
        this.enlaces = enlaces;
        this.alertas = alertas;
        this.identidad = identidad;
        this.ajustes = ajustes;
        this.evidencias = evidencias;
        this.vistas = vistas;
    }

    @Override
    @Transactional
    public String crearEnlace(Long alertId) {
        String codigo = TrackingLink.generarCodigo();
        enlaces.guardar(TrackingLink.para(alertId, codigo));
        return codigo;
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingView consultar(String codigo, LocalDateTime ahora) {
        return vista(alertaVigente(codigo, ahora));
    }

    @Override
    @Transactional
    public TrackingView consultar(String codigo, String visitante, LocalDateTime ahora) {
        AlertSummary alerta = alertaVigente(codigo, ahora);
        if (visitante != null && visitante.matches("[A-Za-z0-9_-]{8,64}")) {
            vistas.registrar(alerta.id(), visitante, ahora);
        }
        return vista(alerta);
    }

    private TrackingView vista(AlertSummary alerta) {
        String nombre = identidad.nombreDePilaDelPerfil(alerta.userProfileId()).orElse("");
        List<LocationPoint> recorrido = new ArrayList<>(alertas.recorrido(alerta.id()));
        if (recorrido.isEmpty() && alerta.latitude() != null && alerta.longitude() != null) {
            recorrido.add(new LocationPoint(alerta.latitude(), alerta.longitude(), null, alerta.startedAt()));
        }
        // Video y audio grabados durante la alerta: el contacto puede verlos para ayudar a ubicarla
        List<EvidenciaPublica> grabaciones = evidencias.deAlerta(alerta.id()).stream()
                .map(e -> new EvidenciaPublica(e.id(), e.mediaType(), e.contentType(), e.createdAt()))
                .toList();
        return new TrackingView(nombre, alerta.status(), "active".equals(alerta.status()), alerta.startedAt(),
                fin(alerta), recorrido, ajustes.numeroEmergencia(), grabaciones);
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceFile evidencia(String codigo, Long evidenciaId, LocalDateTime ahora) {
        AlertSummary alerta = alertaVigente(codigo, ahora);
        return evidencias.archivo(alerta.id(), evidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("La evidencia no existe"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertMessageView> mensajes(String codigo, Long afterId, LocalDateTime ahora) {
        return alertas.mensajes(alertaVigente(codigo, ahora).id(), afterId);
    }

    @Override
    @Transactional
    public AlertMessageView enviarMensaje(String codigo, String nombre, String texto, LocalDateTime ahora) {
        AlertSummary alerta = alertaVigente(codigo, ahora);
        limite.registrar(TrackingLink.hash(codigo), ahora);
        return alertas.mensajeDeContacto(alerta.id(), nombre, texto);
    }

    private AlertSummary alertaVigente(String codigo, LocalDateTime ahora) {
        if (!TrackingLink.formatoPosible(codigo)) throw noDisponible();
        TrackingLink enlace = enlaces.porHash(TrackingLink.hash(codigo)).orElseThrow(SeguimientoPublicoService::noDisponible);
        if (enlace.revocado()) throw noDisponible();
        AlertSummary alerta = alertas.buscar(enlace.getAlertId()).orElseThrow(SeguimientoPublicoService::noDisponible);
        if (!TrackingLink.seguimientoVigente(alerta.status(), fin(alerta), ahora)) throw noDisponible();
        return alerta;
    }

    static LocalDateTime fin(AlertSummary a) {
        return a.endedAt() != null ? a.endedAt() : a.cancelledAt();
    }

    private static ResourceNotFoundException noDisponible() {
        return new ResourceNotFoundException("El enlace de seguimiento no existe o ya venció");
    }
}

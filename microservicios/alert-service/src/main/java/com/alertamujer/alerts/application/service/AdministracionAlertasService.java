package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.application.port.in.AdministrarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase.ContactoNotificado;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Alertas para el panel: los campos de la alerta más la usuaria que la activó (nombre y correo). */
@Service
public class AdministracionAlertasService implements AdministrarAlertasUseCase {

    private final AlertaRepositorio repositorio;
    private final IdentidadPort identidad;
    private final EventosAlertaPort eventos;
    private final UbicacionRepositorio ubicaciones;
    private final DestinatariosAlertaUseCase destinatarios;

    public AdministracionAlertasService(AlertaRepositorio repositorio, IdentidadPort identidad, EventosAlertaPort eventos,
                                        UbicacionRepositorio ubicaciones, DestinatariosAlertaUseCase destinatarios) {
        this.repositorio = repositorio;
        this.identidad = identidad;
        this.eventos = eventos;
        this.ubicaciones = ubicaciones;
        this.destinatarios = destinatarios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAlertView> listar() {
        Map<Long, UserSummary> porPerfil = identidad.usuariasPorPerfil();
        return repositorio.todas().stream()
                .sorted(Comparator.comparing(Alert::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> {
                    UserSummary u = porPerfil.get(a.getUserProfileId());
                    return new AdminAlertView(a.getId(), a.getUserProfileId(), u != null ? u.id() : null,
                            u != null ? u.fullName() : null, u != null ? u.email() : null,
                            a.getDeviceId(), a.getAlertType(), a.getActivationMethod(), a.getStatus(),
                            a.getMessage(), a.getLatitude(), a.getLongitude(),
                            a.getStartedAt(), a.getEndedAt(), a.getCancelledAt(), a.getCreatedAt());
                })
                .toList();
    }

    @Override
    @Transactional
    public Alert cambiarEstado(Long id, String estado) {
        Alert alerta = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        LocalDateTime ahora = LocalDateTime.now();
        if (alerta.cambiarEstado(estado, ahora)) eventos.alertaTerminada(alerta, ahora);
        return repositorio.guardar(alerta);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleAdmin detalle(Long id) {
        Alert alerta = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        boolean visible = alerta.estaActiva() || "resolved".equals(alerta.getStatus());
        List<LocationLog> puntos = visible ? ubicaciones.deLaAlerta(id) : List.of();
        UltimaUbicacion ultima = puntos.stream()
                .max(Comparator.comparing(LocationLog::getRecordedAt))
                .map(l -> new UltimaUbicacion(l.getLatitude(), l.getLongitude(), l.getAccuracy(), l.getRecordedAt()))
                .orElse(null);
        List<ContactoNotificado> contactos = destinatarios.contactosDe(alerta);
        List<EnvioAdmin> envios = new java.util.ArrayList<>();
        for (int i = 0; i < contactos.size(); i++) {
            ContactoNotificado c = contactos.get(i);
            envios.add(new EnvioAdmin(i + 1, c.relationship(), c.channels().stream()
                    .map(k -> new CanalAdmin(k.channel(), k.status(), k.sentAt(), k.deliveredAt())).toList()));
        }
        return new DetalleAdmin(visible, puntos.size(), ultima, envios);
    }
}

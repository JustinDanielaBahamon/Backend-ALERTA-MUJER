package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.application.port.in.AdministrarAlertasUseCase;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
import com.alertamujer.alerts.domain.model.Alert;
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

    public AdministracionAlertasService(AlertaRepositorio repositorio, IdentidadPort identidad, EventosAlertaPort eventos) {
        this.repositorio = repositorio;
        this.identidad = identidad;
        this.eventos = eventos;
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
}

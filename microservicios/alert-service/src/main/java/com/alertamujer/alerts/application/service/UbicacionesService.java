package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NuevaUbicacion;
import com.alertamujer.alerts.application.port.in.UbicacionesUseCase;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.UbicacionRepositorio;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Historial de ubicaciones: del día a día (solo del celular principal) y de cada alerta. */
@Service
public class UbicacionesService implements UbicacionesUseCase {

    private static final int MAXIMO_LIMITE = 500;

    private final UbicacionRepositorio repository;
    private final AlertaRepositorio alerts;
    private final IdentityPort identity;
    private final CelularesPort phones;

    public UbicacionesService(UbicacionRepositorio repository, AlertaRepositorio alerts,
                              IdentityPort identity, CelularesPort phones) {
        this.repository = repository;
        this.alerts = alerts;
        this.identity = identity;
        this.phones = phones;
    }

    @Override
    public Optional<LocationLog> save(NuevaUbicacion newOne, Long userId) {
        Long profile = identity.perfilRequerido(userId);
        LocationLog location = LocationLog.created(profile, newOne.latitude(), newOne.longitude(), newOne.accuracy());
        // Solo se asocia a la alerta si es de la misma usuaria
        if (newOne.alertId() != null) {
            alerts.find(newOne.alertId()).filter(a -> a.esDe(profile)).ifPresent(a -> location.setAlertId(a.getId()));
        }
        if (!location.esDeAlerta() && !phones.esElMasReciente(userId, newOne.deviceId())) {
            return Optional.empty();
        }
        location.registrarHora(newOne.recordedAt(), LocalDateTime.now());
        return Optional.of(repository.save(location));
    }

    @Override
    public List<LocationLog> recientes(Long userId, int limite) {
        return repository.recientes(identity.perfilRequerido(userId), Math.max(1, Math.min(limite, MAXIMO_LIMITE)));
    }

    @Override
    public Optional<LocationLog> ultima(Long userId) {
        return repository.ultima(identity.perfilRequerido(userId));
    }

    @Override
    public List<LocationLog> ofAlert(Long alertId, Long userId, boolean admin) {
        Alert alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        boolean own = alert.esDe(identity.perfilRequerido(userId));
        // La administración solo ve el recorrido de alertas activas o atendidas (auditoría S2):
        // las canceladas o vencidas pudieron ser una falsa alarma y no se revisan
        boolean administracionPuede = admin && (alert.estaActiva() || "resolved".equals(alert.getStatus()));
        if (!own && !administracionPuede) {
            throw new AccessDeniedException("No puedes ver ubicaciones de esta alerta");
        }
        return repository.ofAlert(alertId);
    }

    @Override
    public List<LocationLog> trayecto(Long alertId) {
        return repository.ofAlert(alertId);
    }
}

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
    private final IdentityPort identidad;
    private final CelularesPort celulares;

    public UbicacionesService(UbicacionRepositorio repository, AlertaRepositorio alerts,
                              IdentityPort identidad, CelularesPort celulares) {
        this.repository = repository;
        this.alerts = alerts;
        this.identidad = identidad;
        this.celulares = celulares;
    }

    @Override
    public Optional<LocationLog> save(NuevaUbicacion nueva, Long userId) {
        Long perfil = identidad.perfilRequerido(userId);
        LocationLog ubicacion = LocationLog.nuevo(perfil, nueva.latitude(), nueva.longitude(), nueva.accuracy());
        // Solo se asocia a la alerta si es de la misma usuaria
        if (nueva.alertId() != null) {
            alerts.find(nueva.alertId()).filter(a -> a.esDe(perfil)).ifPresent(a -> ubicacion.setAlertId(a.getId()));
        }
        if (!ubicacion.esDeAlerta() && !celulares.esElMasReciente(userId, nueva.deviceId())) {
            return Optional.empty();
        }
        ubicacion.registrarHora(nueva.recordedAt(), LocalDateTime.now());
        return Optional.of(repository.save(ubicacion));
    }

    @Override
    public List<LocationLog> recientes(Long userId, int limite) {
        return repository.recientes(identidad.perfilRequerido(userId), Math.max(1, Math.min(limite, MAXIMO_LIMITE)));
    }

    @Override
    public Optional<LocationLog> ultima(Long userId) {
        return repository.ultima(identidad.perfilRequerido(userId));
    }

    @Override
    public List<LocationLog> ofAlert(Long alertId, Long userId, boolean admin) {
        Alert alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        boolean propia = alert.esDe(identidad.perfilRequerido(userId));
        // La administración solo ve el recorrido de alertas activas o atendidas (auditoría S2):
        // las canceladas o vencidas pudieron ser una falsa alarma y no se revisan
        boolean administracionPuede = admin && (alert.estaActiva() || "resolved".equals(alert.getStatus()));
        if (!propia && !administracionPuede) {
            throw new AccessDeniedException("No puedes ver ubicaciones de esta alerta");
        }
        return repository.ofAlert(alertId);
    }

    @Override
    public List<LocationLog> trayecto(Long alertId) {
        return repository.ofAlert(alertId);
    }
}

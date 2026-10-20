package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NuevaUbicacion;
import com.alertamujer.alerts.application.port.in.UbicacionesUseCase;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.CelularesPort;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
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

    private final UbicacionRepositorio repositorio;
    private final AlertaRepositorio alertas;
    private final IdentidadPort identidad;
    private final CelularesPort celulares;

    public UbicacionesService(UbicacionRepositorio repositorio, AlertaRepositorio alertas,
                              IdentidadPort identidad, CelularesPort celulares) {
        this.repositorio = repositorio;
        this.alertas = alertas;
        this.identidad = identidad;
        this.celulares = celulares;
    }

    @Override
    public Optional<LocationLog> guardar(NuevaUbicacion nueva, Long userId) {
        Long perfil = identidad.perfilRequerido(userId);
        LocationLog ubicacion = LocationLog.nuevo(perfil, nueva.latitude(), nueva.longitude(), nueva.accuracy());
        // Solo se asocia a la alerta si es de la misma usuaria
        if (nueva.alertId() != null) {
            alertas.buscar(nueva.alertId()).filter(a -> a.esDe(perfil)).ifPresent(a -> ubicacion.setAlertId(a.getId()));
        }
        if (!ubicacion.esDeAlerta() && !celulares.esElMasReciente(userId, nueva.deviceId())) {
            return Optional.empty();
        }
        ubicacion.registrarHora(nueva.recordedAt(), LocalDateTime.now());
        return Optional.of(repositorio.guardar(ubicacion));
    }

    @Override
    public List<LocationLog> recientes(Long userId, int limite) {
        return repositorio.recientes(identidad.perfilRequerido(userId), Math.max(1, Math.min(limite, MAXIMO_LIMITE)));
    }

    @Override
    public Optional<LocationLog> ultima(Long userId) {
        return repositorio.ultima(identidad.perfilRequerido(userId));
    }

    @Override
    public List<LocationLog> deLaAlerta(Long alertId, Long userId, boolean admin) {
        Alert alerta = alertas.buscar(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        boolean propia = alerta.esDe(identidad.perfilRequerido(userId));
        // La administración solo ve el recorrido de alertas activas o atendidas (auditoría S2):
        // las canceladas o vencidas pudieron ser una falsa alarma y no se revisan
        boolean administracionPuede = admin && (alerta.estaActiva() || "resolved".equals(alerta.getStatus()));
        if (!propia && !administracionPuede) {
            throw new AccessDeniedException("No puedes ver ubicaciones de esta alerta");
        }
        return repositorio.deLaAlerta(alertId);
    }

    @Override
    public List<LocationLog> trayecto(Long alertId) {
        return repositorio.deLaAlerta(alertId);
    }
}

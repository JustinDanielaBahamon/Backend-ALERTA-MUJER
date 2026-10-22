package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.LocationLog;

import java.util.List;
import java.util.Optional;

/** Ubicaciones que reporta la app. Son datos muy sensibles: cada usuaria solo ve las suyas. */
public interface UbicacionesUseCase {

    /**
     * Guarda la ubicación. Con varios celulares, la del día a día solo se guarda si viene del que
     * la usuaria usó más recientemente (vacío si se descarta); la de una alerta se guarda siempre.
     */
    Optional<LocationLog> save(NuevaUbicacion newOne, Long userId);

    List<LocationLog> recientes(Long userId, int limite);

    Optional<LocationLog> ultima(Long userId);

    /** Puntos de una alerta: solo la dueña de la alerta o la administración. */
    List<LocationLog> ofAlert(Long alertId, Long userId, boolean admin);

    /** Recorrido de la alerta sin verificar dueña (seguimiento de los contactos). */
    List<LocationLog> trayecto(Long alertId);
}

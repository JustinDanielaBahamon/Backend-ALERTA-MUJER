package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.LocationLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: el historial de ubicaciones. */
public interface UbicacionRepositorio {

    LocationLog save(LocationLog ubicacion);

    /** Las más recientes primero. */
    List<LocationLog> recientes(Long perfil, int cuantas);

    Optional<LocationLog> ultima(Long perfil);

    /** Recorrido de una alerta, del primer punto al último. */
    List<LocationLog> ofAlert(Long alertId);

    /** Borra las ubicaciones del día a día (sin alerta) anteriores a la fecha. */
    int borrarSinAlertaAntesDe(LocalDateTime date);

    /** Borra los recorridos de las alertas anteriores a la fecha. */
    int borrarDeAlertasAntesDe(LocalDateTime date);
}

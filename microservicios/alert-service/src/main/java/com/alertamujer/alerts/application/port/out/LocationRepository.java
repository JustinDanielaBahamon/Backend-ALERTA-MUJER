package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.LocationLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: el historial de ubicaciones. */
public interface LocationRepository {

    LocationLog save(LocationLog location);

    /** Las más recientes primero. */
    List<LocationLog> recent(Long profile, int howMany);

    Optional<LocationLog> latest(Long profile);

    /** Recorrido de una alerta, del primer punto al último. */
    List<LocationLog> ofAlert(Long alertId);

    /** Borra las ubicaciones del día a día (sin alerta) anteriores a la fecha. */
    int deleteWithoutAlertBefore(LocalDateTime date);

    /** Borra los recorridos de las alertas anteriores a la fecha. */
    int deleteFromAlertsBefore(LocalDateTime date);
}

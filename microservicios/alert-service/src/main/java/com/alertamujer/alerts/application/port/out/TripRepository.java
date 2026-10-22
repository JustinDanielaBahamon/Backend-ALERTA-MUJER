package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.SafeTrip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: trayectos de "Avísame si no llego". */
public interface TripRepository {

    SafeTrip save(SafeTrip trip);

    Optional<SafeTrip> find(Long id);

    Optional<SafeTrip> activeOf(Long profile);

    /** Activos cuya hora de llegada es anterior a este momento (para avisar o alertar). */
    List<SafeTrip> activeArrivingBefore(LocalDateTime moment);

    /** Trayectos iniciados por la usuaria desde ese momento (límite diario). */
    long startedSince(Long profile, LocalDateTime since);
}

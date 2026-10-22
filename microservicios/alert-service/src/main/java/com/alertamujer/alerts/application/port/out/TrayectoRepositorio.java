package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.SafeTrip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: trayectos de "Avísame si no llego". */
public interface TrayectoRepositorio {

    SafeTrip save(SafeTrip trayecto);

    Optional<SafeTrip> find(Long id);

    Optional<SafeTrip> activoDe(Long perfil);

    /** Activos cuya hora de llegada es anterior a este momento (para avisar o alertar). */
    List<SafeTrip> activosQueLleganAntesDe(LocalDateTime momento);

    /** Trayectos iniciados por la usuaria desde ese momento (límite diario). */
    long iniciadosDesde(Long perfil, LocalDateTime desde);
}

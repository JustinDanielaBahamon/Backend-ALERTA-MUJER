package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.SafeTrip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output port: "Avísame si no llego" trips. */
public interface TripRepository {

    SafeTrip save(SafeTrip trip);

    Optional<SafeTrip> find(Long id);

    Optional<SafeTrip> activeOf(Long profile);

    /** Active ones whose arrival time is before this moment (to notify or alert). */
    List<SafeTrip> activeArrivingBefore(LocalDateTime moment);

    /** Trips started by the user since that moment (daily limit). */
    long startedSince(Long profile, LocalDateTime since);
}

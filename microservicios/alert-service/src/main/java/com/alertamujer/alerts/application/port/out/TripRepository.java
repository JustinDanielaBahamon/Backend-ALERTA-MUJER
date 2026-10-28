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


    /** All active trips (to check their signals of life). */
    List<SafeTrip> allActive();
    /** Trips started by the user since that moment (daily limit). */
    long startedSince(Long profile, LocalDateTime since);
}

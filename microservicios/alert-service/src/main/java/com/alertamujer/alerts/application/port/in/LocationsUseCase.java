package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.LocationLog;

import java.util.List;
import java.util.Optional;

/** Locations the app reports. They are very sensitive data: each user only sees her own. */
public interface LocationsUseCase {

    /**
     * Stores the location. With several phones, the day-to-day one is only stored if it comes from the
     * phone the user used most recently (empty if discarded); an alert one is always stored.
     */
    Optional<LocationLog> save(NewLocation newOne, Long userId);

    List<LocationLog> recent(Long userId, int limit);

    Optional<LocationLog> latest(Long userId);

    /** Points of an alert: only the alert owner or the administration. */
    List<LocationLog> ofAlert(Long alertId, Long userId, boolean admin);

    /** Alert route without checking the owner (contacts tracking page). */
    List<LocationLog> trip(Long alertId);
}

package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.LocationLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output port: location history. */
public interface LocationRepository {

    LocationLog save(LocationLog location);

    /** Most recent first. */
    List<LocationLog> recent(Long profile, int howMany);

    Optional<LocationLog> latest(Long profile);

    /** Route of an alert, from the first point to the last. */
    List<LocationLog> ofAlert(Long alertId);

    /** Deletes day-to-day locations (without alert) older than the date. */
    int deleteWithoutAlertBefore(LocalDateTime date);

    /** Deletes alert routes older than the date. */
    int deleteFromAlertsBefore(LocalDateTime date);
}

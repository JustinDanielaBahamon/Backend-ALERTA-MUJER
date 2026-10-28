package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.SafeTrip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/** "Avísame si no llego": the user starts, confirms, extends or cancels her trip. */
public interface TripsUseCase {

    /** Only one can be active: if there is another one, it fails (IllegalStateException). */
    SafeTrip start(NewTrip created, Long userId);

    Optional<SafeTrip> active(Long userId);

    SafeTrip arrived(Long id, Long userId);

    SafeTrip extender(Long id, int minutes, Long userId);

    SafeTrip cancel(Long id, Long userId);
    /**
     * Signal of life from her phone during the trip, with its location (also stored as a route point).
     * Points queued while offline that arrive after the server already alerted are added to that alert.
     */
    SafeTrip heartbeat(Long id, Heartbeat point, Long userId);

    record Heartbeat(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt) {}

    record NewTrip(String target, int minutes, Long deviceId, BigDecimal latitude, BigDecimal longitude) {}
}

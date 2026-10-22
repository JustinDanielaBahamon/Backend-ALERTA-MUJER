package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.SafeTrip;

import java.math.BigDecimal;
import java.util.Optional;

/** "Avísame si no llego": la usuaria inicia, confirma, alarga o cancela su trayecto. */
public interface TripsUseCase {

    /** Solo puede haber uno activo: si ya hay otro, falla (IllegalStateException). */
    SafeTrip start(NewTrip created, Long userId);

    Optional<SafeTrip> active(Long userId);

    SafeTrip arrived(Long id, Long userId);

    SafeTrip extender(Long id, int minutes, Long userId);

    SafeTrip cancel(Long id, Long userId);

    record NewTrip(String target, int minutes, Long deviceId, BigDecimal latitude, BigDecimal longitude) {}
}

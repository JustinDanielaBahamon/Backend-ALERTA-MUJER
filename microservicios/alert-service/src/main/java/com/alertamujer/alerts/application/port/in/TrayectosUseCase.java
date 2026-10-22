package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.SafeTrip;

import java.math.BigDecimal;
import java.util.Optional;

/** "Avísame si no llego": la usuaria inicia, confirma, alarga o cancela su trayecto. */
public interface TrayectosUseCase {

    /** Solo puede haber uno activo: si ya hay otro, falla (IllegalStateException). */
    SafeTrip iniciar(NuevoTrayecto created, Long userId);

    Optional<SafeTrip> activo(Long userId);

    SafeTrip llegue(Long id, Long userId);

    SafeTrip extender(Long id, int minutes, Long userId);

    SafeTrip cancelar(Long id, Long userId);

    record NuevoTrayecto(String target, int minutes, Long deviceId, BigDecimal latitude, BigDecimal longitude) {}
}

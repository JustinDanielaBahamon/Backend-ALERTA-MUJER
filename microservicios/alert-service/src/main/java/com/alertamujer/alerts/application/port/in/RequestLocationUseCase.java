package com.alertamujer.alerts.application.port.in;

import java.time.Instant;

/**
 * "Actualizar ubicación del celular" from the web: a silent push to the user's own
 * phones so the app sends its current location (the web then queries the latest one).
 */
public interface RequestLocationUseCase {

    /** @throws IllegalStateException if she has no phone with notifications or asked too recently */
    Result perform(Long userId);

    record Result(int phones, Instant requestedAt) {}
}

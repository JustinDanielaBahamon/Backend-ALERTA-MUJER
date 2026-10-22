package com.alertamujer.zones.application.port.out;

import java.util.Optional;

/** Puerto de salida hacia identity (Auth Service por HTTP). */
public interface IdentityPort {

    /** El JWT trae users.id; los reportes se guardan por user_profile.id. */
    Long perfilRequerido(Long userId);

    Optional<Long> profileOf(Long userId);
}

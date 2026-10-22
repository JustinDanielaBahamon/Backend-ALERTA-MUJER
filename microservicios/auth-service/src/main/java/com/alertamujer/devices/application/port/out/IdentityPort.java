package com.alertamujer.devices.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/** Puerto de salida hacia identity: los dispositivos se guardan por account.id. */
public interface IdentityPort {

    /** account.id de la persona (users.id), si tiene cuenta. */
    Optional<Long> cuentaDe(Long userId);

    /** Como cuentaDe, pero falla con ResourceNotFoundException si no tiene cuenta. */
    Long cuentaRequerida(Long userId);

    /** users.id de la dueña del perfil. */
    Optional<Long> userOfProfile(Long perfil);

    Optional<UserSummary> duenaDeCuenta(Long accountId);

    Map<Long, UserSummary> duenasPorCuenta();
}

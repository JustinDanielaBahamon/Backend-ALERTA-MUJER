package com.alertamujer.resources.application.port.out;

/** Puerto de salida hacia identity (Auth Service por HTTP). */
public interface IdentityPort {

    /** El JWT trae users.id; las llamadas se guardan por user_profile.id. */
    Long perfilRequerido(Long userId);
}

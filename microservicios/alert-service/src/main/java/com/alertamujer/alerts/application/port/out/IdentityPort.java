package com.alertamujer.alerts.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/** Puerto de salida: datos de las usuarias (Auth Service). */
public interface IdentityPort {

    /** El JWT trae users.id; las alertas, contactos y ubicaciones pertenecen a user_profile.id. */
    Optional<Long> profileOf(Long userId);

    /** Como perfilDe, pero falla si la usuaria no tiene perfil. */
    Long perfilRequerido(Long userId);

    Optional<UserSummary> usuaria(Long userId);

    Map<Long, UserSummary> usersByProfile();

    /** true si el correo es de una usuaria registrada (se le puede avisar también por push). */
    boolean tieneCuenta(String correo);

    /** users.id de la persona registrada con ese correo, si existe. */
    Optional<Long> usuariaPorCorreo(String correo);
}

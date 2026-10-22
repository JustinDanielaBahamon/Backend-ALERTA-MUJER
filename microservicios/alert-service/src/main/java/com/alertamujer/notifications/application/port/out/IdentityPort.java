package com.alertamujer.notifications.application.port.out;

import java.util.Optional;

/** Puerto de salida hacia identity (Auth Service por HTTP). */
public interface IdentityPort {

    /** Nombre completo de la dueña del perfil (para los correos). */
    Optional<String> profileFullName(Long profile);

    /** Solo el nombre de pila: el seguimiento puede reenviarse. */
    Optional<String> profileFirstName(Long profile);

    /** users.id de la persona con ese correo, si tiene cuenta. */
    Optional<Long> userWithEmail(String email);

    Optional<String> emailOf(Long userId);

    Optional<Long> profileOf(Long userId);
}

package com.alertamujer.notifications.application.port.out;

import java.util.Optional;

/** Puerto de salida hacia identity (Auth Service por HTTP). */
public interface IdentityPort {

    /** Nombre completo de la dueña del perfil (para los correos). */
    Optional<String> nombreCompletoDelPerfil(Long perfil);

    /** Solo el nombre de pila: el seguimiento puede reenviarse. */
    Optional<String> nombreDePilaDelPerfil(Long perfil);

    /** users.id de la persona con ese correo, si tiene cuenta. */
    Optional<Long> usuariaConCorreo(String correo);

    Optional<String> correoDe(Long userId);

    Optional<Long> profileOf(Long userId);
}

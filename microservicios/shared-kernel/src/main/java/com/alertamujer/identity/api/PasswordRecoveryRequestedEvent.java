package com.alertamujer.identity.api;

/**
 * Se publica cuando alguien pide recuperar la contraseña de una cuenta existente. El módulo de
 * notificaciones lo escucha y envía el código por correo. El código solo viaja en memoria y en el
 * correo: en la base queda su hash.
 */
public record PasswordRecoveryRequestedEvent(String email, String name, @com.fasterxml.jackson.annotation.JsonProperty("codigo") String code, @com.fasterxml.jackson.annotation.JsonProperty("minutosValidez") int validityMinutes) {}

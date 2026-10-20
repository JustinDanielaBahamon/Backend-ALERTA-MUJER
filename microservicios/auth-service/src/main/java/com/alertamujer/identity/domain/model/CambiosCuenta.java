package com.alertamujer.identity.domain.model;

/** Cambios de la administración sobre una persona y su cuenta. Un campo null no se toca. */
public record CambiosCuenta(String accountStatus, String firstName, String lastName, String telephone, String email) {
}

package com.alertamujer.identity.api;

import java.time.LocalDateTime;

/** Datos de una persona que otros módulos pueden conocer (sin credenciales ni documentos). */
public record UserSummary(Long id, String firstName, String lastName, String email, String telephone,
                          Long roleId, LocalDateTime createdAt) {

    public static final long ROL_ADMIN = 2L;

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    public boolean admin() {
        return roleId != null && roleId == ROL_ADMIN;
    }
}

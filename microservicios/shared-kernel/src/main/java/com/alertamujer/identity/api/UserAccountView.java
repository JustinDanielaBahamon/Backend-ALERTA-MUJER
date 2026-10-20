package com.alertamujer.identity.api;

import java.time.LocalDateTime;

/** Persona con su cuenta, para el panel de administración. profileId es null si no tiene perfil de app. */
public record UserAccountView(Long userId, Long profileId, String firstName, String lastName, String email,
                              String telephone, String roleName, String accountStatus, LocalDateTime createdAt,
                              LocalDateTime lastAccess) {}

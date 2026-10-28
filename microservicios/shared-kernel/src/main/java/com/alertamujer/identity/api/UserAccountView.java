package com.alertamujer.identity.api;

import java.time.LocalDateTime;

/** Person with her account, for the admin panel. profileId is null if she has no app profile. */
public record UserAccountView(Long userId, Long profileId, String firstName, String lastName, String email,
                              String telephone, String roleName, String accountStatus, LocalDateTime createdAt,
                              LocalDateTime lastAccess) {}

package com.alertamujer.admin.infrastructure.controller.dto;

import java.time.LocalDateTime;

/**
 * DTO for admin user list view.
 * Maps database fields to expected frontend format without exposing JPA entities.
 */
public record AdminUserDto(
        Long id,
        Long userProfileId,
        String firstName,
        String lastName,
        String email,
        String telephone,
        String role,
        String accountStatus,
        LocalDateTime createdAt,
        LocalDateTime lastAccess,
        Long alertCount
) {
}

package com.alertamujer.admin.infrastructure.controller.dto;

/**
 * Request DTO for updating user information.
 * All fields are optional to support partial updates.
 */
public record UpdateUserRequest(
        String accountStatus,
        String firstName,
        String lastName,
        String telephone,
        String email
) {
}

package com.alertamujer.identity.infrastructure.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyResetCodeRequest(
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email debe tener un formato válido") String email,
        @NotBlank(message = "El código es obligatorio") @Pattern(regexp = "\\d{6}", message = "El código tiene 6 dígitos") String code) {}

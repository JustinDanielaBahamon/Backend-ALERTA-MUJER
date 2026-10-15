package com.alertamujer.identity.infrastructure.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Token de identidad de Google (móvil) o de Firebase Authentication (web). */
@Data
public class GoogleLoginRequest {
    @NotBlank
    private String idToken;
}

package com.alertamujer.identity.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Token de identidad de Google (móvil) o de Firebase Authentication (web). */
@Data
public class GoogleLoginRequest {
    @NotBlank
    private String idToken;
}

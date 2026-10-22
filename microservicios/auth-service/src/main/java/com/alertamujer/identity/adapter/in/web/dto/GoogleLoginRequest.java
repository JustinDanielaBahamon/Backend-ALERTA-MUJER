package com.alertamujer.identity.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Google identity token (mobile) or Firebase Authentication token (web). */
@Data
public class GoogleLoginRequest {
    @NotBlank
    private String idToken;
}

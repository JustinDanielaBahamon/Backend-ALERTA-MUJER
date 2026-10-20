package com.alertamujer.identity.adapter.in.web.dto;

import com.alertamujer.identity.application.port.in.Sesion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserResponse user;

    public static AuthResponse of(Sesion s) {
        return new AuthResponse(s.token(), s.refreshToken(), UserResponse.of(s.usuaria(), null));
    }
}

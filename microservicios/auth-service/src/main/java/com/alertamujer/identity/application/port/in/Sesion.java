package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.User;

/** Resultado de entrar o registrarse: los dos tokens y la persona. */
public record Sesion(String token, String refreshToken, User usuaria) {
}

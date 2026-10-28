package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.User;

/** Result of signing in or registering: both tokens and the person. */
public record Session(String token, String refreshToken, User user) {
}

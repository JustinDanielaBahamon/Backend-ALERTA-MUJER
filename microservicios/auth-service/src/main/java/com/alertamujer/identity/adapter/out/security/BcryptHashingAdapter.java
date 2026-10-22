package com.alertamujer.identity.adapter.out.security;

import com.alertamujer.identity.application.port.out.HashingPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador de salida: contraseñas con BCrypt (Spring Security). */
@Component
class BcryptHashingAdapter implements HashingPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String password) {
        return encoder.encode(password);
    }

    @Override
    public boolean coincide(String password, String hash) {
        return password != null && hash != null && encoder.matches(password, hash);
    }
}

package com.alertamujer.identity.adapter.out.seguridad;

import com.alertamujer.identity.application.port.out.CifradoPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador de salida: contraseñas con BCrypt (Spring Security). */
@Component
class CifradoBcryptAdapter implements CifradoPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String contrasena) {
        return encoder.encode(contrasena);
    }

    @Override
    public boolean coincide(String contrasena, String hash) {
        return contrasena != null && hash != null && encoder.matches(contrasena, hash);
    }
}

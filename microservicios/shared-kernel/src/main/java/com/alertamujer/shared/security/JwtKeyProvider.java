package com.alertamujer.shared.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Key to sign and verify JWTs, read from JWT_SECRET. No default value: the code used to ship one
 * and, if the variable was missing on the server, anyone who saw the repository could forge
 * tokens. Now the backend does not start if it is missing, too short or that value.
 */
@Component
public class JwtKeyProvider {

    /** Value that was published in the repository: must never be used. */
    static final String PUBLISHED_SECRET = "alerta_mujer_jwt_secret_key_2026_xK9mP2qR5sT8uV1wX4yZ7";
    static final int MIN_LENGTH = 32;

    private final SecretKey key;

    public JwtKeyProvider(@Value("${jwt.secret:}") String secret) {
        this.key = Keys.hmacShaKeyFor(validate(secret).getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey key() {
        return key;
    }

    static String validate(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta JWT_SECRET: define un secreto aleatorio de al menos 32 caracteres");
        }
        if (secret.length() < MIN_LENGTH) {
            throw new IllegalStateException("JWT_SECRET es demasiado corto: usa al menos 32 caracteres");
        }
        if (PUBLISHED_SECRET.equals(secret)) {
            throw new IllegalStateException("JWT_SECRET es el valor que estuvo publicado en el repositorio: genera uno nuevo");
        }
        return secret;
    }
}

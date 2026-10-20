package com.alertamujer.shared.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Clave para firmar y verificar los JWT, leída de JWT_SECRET. Sin valor por defecto: antes el
 * código traía uno escrito y, si la variable faltaba en el servidor, cualquiera que viera el
 * repositorio podía fabricar tokens. Ahora el backend no arranca si falta, es corta o es ese valor.
 */
@Component
public class JwtKeyProvider {

    /** Valor que estuvo publicado en el repositorio: nunca debe usarse. */
    static final String SECRETO_PUBLICADO = "alerta_mujer_jwt_secret_key_2026_xK9mP2qR5sT8uV1wX4yZ7";
    static final int LARGO_MINIMO = 32;

    private final SecretKey key;

    public JwtKeyProvider(@Value("${jwt.secret:}") String secreto) {
        this.key = Keys.hmacShaKeyFor(validar(secreto).getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey key() {
        return key;
    }

    static String validar(String secreto) {
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException("Falta JWT_SECRET: define un secreto aleatorio de al menos 32 caracteres");
        }
        if (secreto.length() < LARGO_MINIMO) {
            throw new IllegalStateException("JWT_SECRET es demasiado corto: usa al menos 32 caracteres");
        }
        if (SECRETO_PUBLICADO.equals(secreto)) {
            throw new IllegalStateException("JWT_SECRET es el valor que estuvo publicado en el repositorio: genera uno nuevo");
        }
        return secreto;
    }
}

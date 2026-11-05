package com.alertamujer.shared.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * Key to sign and verify JWTs, read from JWT_SECRET. No default value: the code used to ship one
 * and, if the variable was missing on the server, anyone who saw the repository could forge
 * tokens. Now the backend does not start if it is missing, too short or that value.
 */
@Component
public class JwtKeyProvider {

    /**
     * SHA-256 of the JWT secrets that were once committed to the repository (the published default
     * and the old examples). Only the hashes are kept, so the code no longer contains those values.
     */
    static final Set<String> BURNED_SECRET_HASHES = Set.of(
            "d31a82ab066b860fb41e84f4a6f17682de3a005bca3c837abedc2c41fa59149e",
            "042f970c0c86195a78c3afd00e52f45f703e3832b3722d8f39a501db7a89d853",
            "8e6b39c9850662d7d49d0730e41134fbd1ef4cec5a2c6b7a101ffd9cb5afb7b6",
            "eddfeb3c47d6a260c9530b9fe6bee0a23ee8f085ab4a2bec0b204560efb7cff6");
    static final int MIN_LENGTH = 32;

    private final SecretKey key;

    public JwtKeyProvider(@Value("${jwt.secret:}") String secret) {
        this.key = Keys.hmacShaKeyFor(validate(secret).getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey key() {
        return key;
    }

    static String validate(String secret) {
        return validate(secret, BURNED_SECRET_HASHES);
    }

    static String validate(String secret, Set<String> burnedHashes) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Falta JWT_SECRET: define un secreto aleatorio de al menos 32 caracteres");
        }
        if (secret.length() < MIN_LENGTH) {
            throw new IllegalStateException("JWT_SECRET es demasiado corto: usa al menos 32 caracteres");
        }
        if (burnedHashes.contains(sha256(secret))) {
            throw new IllegalStateException("JWT_SECRET es el valor que estuvo publicado en el repositorio: genera uno nuevo");
        }
        return secret;
    }

    static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}

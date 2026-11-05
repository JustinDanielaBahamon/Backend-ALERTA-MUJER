package com.alertamujer.shared.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtKeyProviderTest {

    @Test
    void rejectsAnEmptyOrShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtKeyProvider(""));
        assertThrows(IllegalStateException.class, () -> new JwtKeyProvider("corto"));
    }

    @Test
    void rejectsASecretWhoseHashIsBurned() {
        String leaked = "a-secret-that-was-committed-to-the-repository-once";
        Set<String> burned = Set.of(JwtKeyProvider.sha256(leaked));
        assertThrows(IllegalStateException.class, () -> JwtKeyProvider.validate(leaked, burned));
        assertEquals("another-random-secret-of-more-than-32-chars",
                JwtKeyProvider.validate("another-random-secret-of-more-than-32-chars", burned));
    }

    @Test
    void keepsOnlyHashesOfTheBurnedSecrets() {
        assertEquals(4, JwtKeyProvider.BURNED_SECRET_HASHES.size());
        JwtKeyProvider.BURNED_SECRET_HASHES.forEach(h -> assertTrue(h.matches("[0-9a-f]{64}")));
    }

    @Test
    void acceptsALongOwnSecret() {
        assertNotNull(new JwtKeyProvider("un-secreto-aleatorio-de-prueba-con-mas-de-32-caracteres").key());
    }
}

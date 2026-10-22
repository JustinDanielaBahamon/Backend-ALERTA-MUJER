package com.alertamujer.shared.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtKeyProviderTest {

    @Test
    void rejectsAnEmptyShortOrPublishedSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtKeyProvider(""));
        assertThrows(IllegalStateException.class, () -> new JwtKeyProvider("corto"));
        assertThrows(IllegalStateException.class, () -> new JwtKeyProvider(JwtKeyProvider.PUBLISHED_SECRET));
    }

    @Test
    void acceptsALongOwnSecret() {
        assertNotNull(new JwtKeyProvider("un-secreto-aleatorio-de-prueba-con-mas-de-32-caracteres").key());
    }
}

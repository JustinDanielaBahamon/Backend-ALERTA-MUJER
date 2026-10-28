package com.alertamujer.identity.adapter.out.security;

import com.alertamujer.shared.security.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokensAdapterTest {

    private final JwtKeyProvider keys = new JwtKeyProvider("clave-de-prueba-solo-para-tests-0123456789abcdef");
    private final JwtTokensAdapter tokens = new JwtTokensAdapter(keys);

    @Test
    void theAccessTokenCarriesThePersonAndHerRole() {
        Claims c = Jwts.parser().verifyWith(keys.key()).build().parseSignedClaims(tokens.access(7L, "admin")).getPayload();

        assertEquals("7", c.getSubject());
        assertEquals("admin", c.get("role", String.class));
    }

    @Test
    void onlyARefreshTokenCanRefresh() {
        assertEquals(7L, tokens.userForRefresh(tokens.refresh(7L)));
        assertThrows(IllegalArgumentException.class, () -> tokens.userForRefresh(tokens.access(7L, "user")));
        assertThrows(RuntimeException.class, () -> tokens.userForRefresh("basura"));
    }

    @Test
    void aTokenSignedWithAnotherKeyDoesNotWork() {
        JwtTokensAdapter other = new JwtTokensAdapter(new JwtKeyProvider("otra-clave-de-prueba-solo-para-tests-abcdef0123"));

        assertThrows(RuntimeException.class, () -> tokens.userForRefresh(other.refresh(7L)));
    }
}

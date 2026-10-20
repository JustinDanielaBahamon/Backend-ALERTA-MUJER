package com.alertamujer.identity.adapter.out.seguridad;

import com.alertamujer.shared.security.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokensAdapterTest {

    private final JwtKeyProvider claves = new JwtKeyProvider("clave-de-prueba-solo-para-tests-0123456789abcdef");
    private final JwtTokensAdapter tokens = new JwtTokensAdapter(claves);

    @Test
    void elTokenDeAccesoLlevaLaPersonaYSuRol() {
        Claims c = Jwts.parser().verifyWith(claves.key()).build().parseSignedClaims(tokens.acceso(7L, "admin")).getPayload();

        assertEquals("7", c.getSubject());
        assertEquals("admin", c.get("role", String.class));
    }

    @Test
    void soloUnTokenDeRenovacionSirveParaRenovar() {
        assertEquals(7L, tokens.usuariaDeRenovacion(tokens.renovacion(7L)));
        assertThrows(IllegalArgumentException.class, () -> tokens.usuariaDeRenovacion(tokens.acceso(7L, "user")));
        assertThrows(RuntimeException.class, () -> tokens.usuariaDeRenovacion("basura"));
    }

    @Test
    void unTokenFirmadoConOtraClaveNoSirve() {
        JwtTokensAdapter otro = new JwtTokensAdapter(new JwtKeyProvider("otra-clave-de-prueba-solo-para-tests-abcdef0123"));

        assertThrows(RuntimeException.class, () -> tokens.usuariaDeRenovacion(otro.renovacion(7L)));
    }
}

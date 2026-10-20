package com.alertamujer.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RecoveryRequestTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void guardaSoloElHashYValeQuinceMinutos() {
        RecoveryRequest r = RecoveryRequest.nueva(4L, "123456", "1.2.3.4", AHORA);

        assertNotEquals("123456", r.getTokenHash());
        assertEquals(64, r.getTokenHash().length());
        assertTrue(r.coincide("123456"));
        assertFalse(r.coincide("654321"));
        assertFalse(r.vencida(AHORA.plusMinutes(14)));
        assertTrue(r.vencida(AHORA.plusMinutes(16)));
        assertEquals("pending", r.getStatus());
    }

    @Test
    void elMismoCodigoEnOtraCuentaDaOtroHash() {
        assertNotEquals(RecoveryRequest.hash(1L, "123456"), RecoveryRequest.hash(2L, "123456"));
    }

    @Test
    void alQuintoFalloElCodigoDejaDeServir() {
        RecoveryRequest r = RecoveryRequest.nueva(4L, "123456", null, AHORA);
        for (int i = 0; i < RecoveryRequest.MAX_INTENTOS - 1; i++) r.registrarFallo();
        assertEquals("pending", r.getStatus());

        r.registrarFallo();
        assertEquals("expired", r.getStatus());
    }

    @Test
    void esRecienteDuranteUnMinuto() {
        RecoveryRequest r = RecoveryRequest.nueva(4L, "123456", null, AHORA);
        assertTrue(r.reciente(AHORA.plusSeconds(30)));
        assertFalse(r.reciente(AHORA.plusSeconds(61)));
    }
}

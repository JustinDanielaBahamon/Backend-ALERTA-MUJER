package com.alertamujer.notifications.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas del enlace de seguimiento, sin Spring ni base de datos. */
class TrackingLinkTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void elCodigoEsAleatorioYSoloSeGuardaSuHash() {
        String code = TrackingLink.generarCodigo();

        assertEquals(43, code.length());
        assertNotEquals(code, TrackingLink.generarCodigo());
        TrackingLink enlace = TrackingLink.para(5L, code);
        assertEquals(5L, enlace.getAlertId());
        assertEquals(TrackingLink.hash(code), enlace.getTokenHash());
        assertFalse(enlace.getTokenHash().contains(code));
        assertFalse(enlace.revocado());
    }

    @Test
    void descartaCodigosQueNoPuedenSerValidos() {
        assertFalse(TrackingLink.formatoPosible(null));
        assertFalse(TrackingLink.formatoPosible("corto"));
        assertFalse(TrackingLink.formatoPosible("x".repeat(101)));
        assertTrue(TrackingLink.formatoPosible(TrackingLink.generarCodigo()));
    }

    @Test
    void elSeguimientoValeHastaDosHorasDespuesDeTerminar() {
        assertTrue(TrackingLink.seguimientoVigente("active", null, NOW));
        assertTrue(TrackingLink.seguimientoVigente("resolved", NOW.minusMinutes(119), NOW));
        assertFalse(TrackingLink.seguimientoVigente("resolved", NOW.minusMinutes(121), NOW));
        assertTrue(TrackingLink.seguimientoVigente("cancelled", null, NOW));
    }
}

package com.alertamujer.notifications.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas del enlace de seguimiento, sin Spring ni base de datos. */
class TrackingLinkTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void elCodigoEsAleatorioYSoloSeGuardaSuHash() {
        String codigo = TrackingLink.generarCodigo();

        assertEquals(43, codigo.length());
        assertNotEquals(codigo, TrackingLink.generarCodigo());
        TrackingLink enlace = TrackingLink.para(5L, codigo);
        assertEquals(5L, enlace.getAlertId());
        assertEquals(TrackingLink.hash(codigo), enlace.getTokenHash());
        assertFalse(enlace.getTokenHash().contains(codigo));
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
        assertTrue(TrackingLink.seguimientoVigente("active", null, AHORA));
        assertTrue(TrackingLink.seguimientoVigente("resolved", AHORA.minusMinutes(119), AHORA));
        assertFalse(TrackingLink.seguimientoVigente("resolved", AHORA.minusMinutes(121), AHORA));
        assertTrue(TrackingLink.seguimientoVigente("cancelled", null, AHORA));
    }
}

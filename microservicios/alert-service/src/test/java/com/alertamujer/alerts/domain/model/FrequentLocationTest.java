package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FrequentLocationTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void unLugarNuevoEsActivoConRiesgoModerado() {
        FrequentLocation l = FrequentLocation.nuevo(10L,
                new DatosLugar("Casa", null, null, BigDecimal.ONE, BigDecimal.ONE, null, " ", null), AHORA);

        assertEquals(10L, l.getUserProfileId());
        assertEquals(FrequentLocation.RIESGO_POR_DEFECTO, l.getRiskLevel());
        assertTrue(l.getIsActive());
        assertEquals(AHORA, l.getCreatedAt());
    }

    @Test
    void actualizarNoBorraLoQueNoSeEnvia() {
        FrequentLocation l = FrequentLocation.nuevo(10L,
                new DatosLugar("Casa", "Calle 1", "Bogotá", BigDecimal.ONE, BigDecimal.ONE, "Notas", "alta", true), AHORA);

        l.actualizar(new DatosLugar(null, null, null, null, null, null, null, false), AHORA.plusHours(1));

        assertEquals("Casa", l.getName());
        assertEquals("Calle 1", l.getAddress());
        assertEquals("Bogotá", l.getCity());
        assertEquals("Notas", l.getNotes());
        assertEquals("alta", l.getRiskLevel());
        assertFalse(l.getIsActive());
        assertEquals(AHORA.plusHours(1), l.getUpdatedAt());
    }
}

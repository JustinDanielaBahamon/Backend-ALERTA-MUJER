package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FrequentLocationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void aNewPlaceIsActiveWithModerateRisk() {
        FrequentLocation l = FrequentLocation.created(10L,
                new PlaceData("Casa", null, null, BigDecimal.ONE, BigDecimal.ONE, null, " ", null), NOW);

        assertEquals(10L, l.getUserProfileId());
        assertEquals(FrequentLocation.DEFAULT_RISK, l.getRiskLevel());
        assertTrue(l.getIsActive());
        assertEquals(NOW, l.getCreatedAt());
    }

    @Test
    void updateDoesNotEraseWhatIsNotSent() {
        FrequentLocation l = FrequentLocation.created(10L,
                new PlaceData("Casa", "Calle 1", "Bogotá", BigDecimal.ONE, BigDecimal.ONE, "Notas", "alta", true), NOW);

        l.update(new PlaceData(null, null, null, null, null, null, null, false), NOW.plusHours(1));

        assertEquals("Casa", l.getName());
        assertEquals("Calle 1", l.getAddress());
        assertEquals("Bogotá", l.getCity());
        assertEquals("Notas", l.getNotes());
        assertEquals("alta", l.getRiskLevel());
        assertFalse(l.getIsActive());
        assertEquals(NOW.plusHours(1), l.getUpdatedAt());
    }
}

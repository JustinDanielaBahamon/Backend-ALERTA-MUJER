package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LocationLogTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void rechazaCoordenadasFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> LocationLog.created(10L, null, BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class, () -> LocationLog.created(10L, BigDecimal.valueOf(91), BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class, () -> LocationLog.created(10L, BigDecimal.ONE, BigDecimal.valueOf(-181), null));
        assertFalse(LocationLog.created(10L, BigDecimal.valueOf(4.6), BigDecimal.valueOf(-74.1), null).esDeAlerta());
    }

    @Test
    void respetaLaHoraDeUnPuntoEnColaSoloSiEsDeLasUltimas24Horas() {
        LocationLog l = LocationLog.created(10L, BigDecimal.ONE, BigDecimal.ONE, null);

        l.registrarHora(NOW.minusHours(2), NOW);
        assertEquals(NOW.minusHours(2), l.getRecordedAt());

        l.registrarHora(NOW.minusHours(30), NOW);
        assertEquals(NOW, l.getRecordedAt());

        l.registrarHora(NOW.plusMinutes(5), NOW);
        assertEquals(NOW, l.getRecordedAt());

        l.registrarHora(null, NOW);
        assertEquals(NOW, l.getRecordedAt());
    }
}

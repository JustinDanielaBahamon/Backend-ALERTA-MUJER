package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LocationLogTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void rechazaCoordenadasFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> LocationLog.nuevo(10L, null, BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class, () -> LocationLog.nuevo(10L, BigDecimal.valueOf(91), BigDecimal.ONE, null));
        assertThrows(IllegalArgumentException.class, () -> LocationLog.nuevo(10L, BigDecimal.ONE, BigDecimal.valueOf(-181), null));
        assertFalse(LocationLog.nuevo(10L, BigDecimal.valueOf(4.6), BigDecimal.valueOf(-74.1), null).esDeAlerta());
    }

    @Test
    void respetaLaHoraDeUnPuntoEnColaSoloSiEsDeLasUltimas24Horas() {
        LocationLog l = LocationLog.nuevo(10L, BigDecimal.ONE, BigDecimal.ONE, null);

        l.registrarHora(AHORA.minusHours(2), AHORA);
        assertEquals(AHORA.minusHours(2), l.getRecordedAt());

        l.registrarHora(AHORA.minusHours(30), AHORA);
        assertEquals(AHORA, l.getRecordedAt());

        l.registrarHora(AHORA.plusMinutes(5), AHORA);
        assertEquals(AHORA, l.getRecordedAt());

        l.registrarHora(null, AHORA);
        assertEquals(AHORA, l.getRecordedAt());
    }
}

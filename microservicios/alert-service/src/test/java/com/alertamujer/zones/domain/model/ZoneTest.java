package com.alertamujer.zones.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de zonas y reportes, sin Spring ni base de datos. */
class ZoneTest {

    private static DatosZona data(String riesgo, Boolean activa) {
        return new DatosZona(2, "Parque", "risk", riesgo, null, null, "Bogotá", BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.TEN, activa);
    }

    @Test
    void unaZonaNuevaTieneRiesgoMedioYEstaActiva() {
        Zone z = Zone.nueva(data(null, null));

        assertEquals("medium", z.getRiskLevel());
        assertTrue(z.activa());
        assertEquals(2, z.getCreatedByAdminId());
    }

    @Test
    void editarReemplazaLosDatos() {
        Zone z = Zone.nueva(data("high", true));
        z.reemplazar(data("low", false));

        assertEquals("low", z.getRiskLevel());
        assertFalse(z.activa());
    }

    @Test
    void unReportePerteneceASuAutoraYSeRevisa() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 20, 10, 0);
        ZoneReport r = ZoneReport.nuevo(10L, 5L, "insegura", "Sin luz");

        assertEquals("pending", r.getStatus());
        assertTrue(r.esDe(10L));
        assertFalse(r.esDe(20L));
        r.aprobar(2, ahora);
        assertEquals("approved", r.getStatus());
        assertEquals(2, r.getReviewedByAdminId());
        assertEquals(ahora, r.getReviewedAt());
        r.rechazar(3, ahora);
        assertEquals("rejected", r.getStatus());
    }
}

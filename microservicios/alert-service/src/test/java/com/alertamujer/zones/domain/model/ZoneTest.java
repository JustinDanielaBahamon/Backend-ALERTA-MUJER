package com.alertamujer.zones.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de zonas y reportes, sin Spring ni base de datos. */
class ZoneTest {

    private static ZoneData data(String risk, Boolean active) {
        return new ZoneData(2, "Parque", "risk", risk, null, null, "Bogotá", BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.TEN, active);
    }

    @Test
    void aNewZoneHasMediumRiskAndIsActive() {
        Zone z = Zone.newOne(data(null, null));

        assertEquals("medium", z.getRiskLevel());
        assertTrue(z.active());
        assertEquals(2, z.getCreatedByAdminId());
    }

    @Test
    void editReplacesTheData() {
        Zone z = Zone.newOne(data("high", true));
        z.replace(data("low", false));

        assertEquals("low", z.getRiskLevel());
        assertFalse(z.active());
    }

    @Test
    void aReportBelongsToItsAuthorAndIsReviewed() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 20, 10, 0);
        ZoneReport r = ZoneReport.created(10L, 5L, "insegura", "Sin luz");

        assertEquals("pending", r.getStatus());
        assertTrue(r.esDe(10L));
        assertFalse(r.esDe(20L));
        r.approve(2, now);
        assertEquals("approved", r.getStatus());
        assertEquals(2, r.getReviewedByAdminId());
        assertEquals(now, r.getReviewedAt());
        r.reject(3, now);
        assertEquals("rejected", r.getStatus());
    }
}

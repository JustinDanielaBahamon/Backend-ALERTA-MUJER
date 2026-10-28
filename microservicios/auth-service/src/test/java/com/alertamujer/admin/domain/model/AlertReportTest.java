package com.alertamujer.admin.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlertReportTest {

    private static final LocalDateTime DAY = LocalDateTime.of(2026, 10, 15, 12, 0);

    private static ReportExportChanges changes(String name, List<Long> ids, String alertStatus) {
        return new ReportExportChanges(name, null, null, null, null, null, null, ids, DAY.minusDays(1), DAY.plusDays(1), alertStatus);
    }

    @Test
    void needsANameAndValidatesStatusFormatAndFilter() {
        assertThrows(IllegalArgumentException.class, () -> AlertReport.created(changes(" ", null, null), 1L));
        AlertReport r = AlertReport.created(changes(" Informe ", null, null), 1L);
        assertEquals("Informe", r.getName());
        assertEquals("pending", r.getStatus());
        assertEquals("PDF", r.getFormat());

        assertThrows(IllegalArgumentException.class, () -> r.apply(new ReportExportChanges(null, null, "raro", null, null, null, null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> r.apply(new ReportExportChanges(null, null, null, null, null, null, "DOC", null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> r.apply(new ReportExportChanges(null, null, null, null, null, null, null, null, null, null, "rara")));
    }

    @Test
    void withoutChosenAlertsUsesTheFilters() {
        AlertReport r = AlertReport.created(changes("Informe", null, "resolved"), 1L);

        assertTrue(r.includes(1L, DAY, "resolved"));
        assertFalse(r.includes(2L, DAY, "active"));
        assertFalse(r.includes(3L, DAY.plusDays(3), "resolved"));
        assertFalse(r.includes(4L, null, "resolved"));
    }

    @Test
    void withChosenAlertsOnlyIncludesThose() {
        AlertReport r = AlertReport.created(changes("Informe", List.of(3L, 1L), "resolved"), 1L);

        assertEquals(Set.of(1L, 3L), r.chosenIds());
        assertTrue(r.includes(3L, DAY.plusDays(30), "active"));
        assertFalse(r.includes(2L, DAY, "resolved"));
    }

    @Test
    void anEmptyCityOrFilterIsDroppedAndExportsAreCounted() {
        AlertReport r = AlertReport.created(changes("Informe", null, "resolved"), 1L);
        r.apply(new ReportExportChanges(null, " ", null, null, null, null, null, null, null, null, ""));
        r.recordExport();
        r.recordExport();

        assertNull(r.getCity());
        assertNull(r.getAlertStatus());
        assertEquals(2, r.getExportCount());
    }

    @Test
    void theSessionEventAddsTheDeviceAndTrimsTheDescription() {
        AuditLog r = AuditLog.sessionEvent(4L, "LOGIN_FAILED", "Contraseña incorrecta", "Chrome · Linux", "1.2.3.4", "Panel web");
        assertEquals("SESSION", r.getEntityType());
        assertEquals("Contraseña incorrecta · Chrome · Linux", r.getDescription());
        assertEquals(500, AuditLog.sessionEvent(4L, "X", "x".repeat(600), null, null, null).getDescription().length());
        assertNull(AuditLog.sessionEvent(4L, "X", null, null, null, null).getDescription());
    }
}

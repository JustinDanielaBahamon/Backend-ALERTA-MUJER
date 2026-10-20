package com.alertamujer.admin.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlertReportTest {

    private static final LocalDateTime DIA = LocalDateTime.of(2026, 10, 15, 12, 0);

    private static CambiosInforme cambios(String nombre, List<Long> ids, String estadoAlerta) {
        return new CambiosInforme(nombre, null, null, null, null, null, null, ids, DIA.minusDays(1), DIA.plusDays(1), estadoAlerta);
    }

    @Test
    void necesitaNombreYValidaEstadoFormatoYFiltro() {
        assertThrows(IllegalArgumentException.class, () -> AlertReport.nuevo(cambios(" ", null, null), 1L));
        AlertReport r = AlertReport.nuevo(cambios(" Informe ", null, null), 1L);
        assertEquals("Informe", r.getName());
        assertEquals("pending", r.getStatus());
        assertEquals("PDF", r.getFormat());

        assertThrows(IllegalArgumentException.class, () -> r.aplicar(new CambiosInforme(null, null, "raro", null, null, null, null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> r.aplicar(new CambiosInforme(null, null, null, null, null, null, "DOC", null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> r.aplicar(new CambiosInforme(null, null, null, null, null, null, null, null, null, null, "rara")));
    }

    @Test
    void sinAlertasElegidasUsaLosFiltros() {
        AlertReport r = AlertReport.nuevo(cambios("Informe", null, "resolved"), 1L);

        assertTrue(r.incluye(1L, DIA, "resolved"));
        assertFalse(r.incluye(2L, DIA, "active"));
        assertFalse(r.incluye(3L, DIA.plusDays(3), "resolved"));
        assertFalse(r.incluye(4L, null, "resolved"));
    }

    @Test
    void conAlertasElegidasSoloIncluyeEsas() {
        AlertReport r = AlertReport.nuevo(cambios("Informe", List.of(3L, 1L), "resolved"), 1L);

        assertEquals(Set.of(1L, 3L), r.idsElegidos());
        assertTrue(r.incluye(3L, DIA.plusDays(30), "active"));
        assertFalse(r.incluye(2L, DIA, "resolved"));
    }

    @Test
    void unaCiudadOFiltroVacioSeQuitanYLasExportacionesSeCuentan() {
        AlertReport r = AlertReport.nuevo(cambios("Informe", null, "resolved"), 1L);
        r.aplicar(new CambiosInforme(null, " ", null, null, null, null, null, null, null, null, ""));
        r.registrarExportacion();
        r.registrarExportacion();

        assertNull(r.getCity());
        assertNull(r.getAlertStatus());
        assertEquals(2, r.getExportCount());
    }

    @Test
    void elEventoDeSesionAgregaElDispositivoYCortaLaDescripcion() {
        AuditLog r = AuditLog.eventoDeSesion(4L, "LOGIN_FAILED", "Contraseña incorrecta", "Chrome · Linux", "1.2.3.4", "Panel web");
        assertEquals("SESSION", r.getEntityType());
        assertEquals("Contraseña incorrecta · Chrome · Linux", r.getDescription());
        assertEquals(500, AuditLog.eventoDeSesion(4L, "X", "x".repeat(600), null, null, null).getDescription().length());
        assertNull(AuditLog.eventoDeSesion(4L, "X", null, null, null, null).getDescription());
    }
}

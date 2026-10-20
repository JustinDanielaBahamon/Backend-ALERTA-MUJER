package com.alertamujer.admin.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la moderación, sin Spring ni base de datos. */
class ModeracionTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void lasExplicacionesTienenMinimoDiezCaracteres() {
        assertThrows(IllegalArgumentException.class, () -> TextoModeracion.explicacion("corto", "mensaje"));
        assertThrows(IllegalArgumentException.class, () -> TextoModeracion.explicacion(null, "mensaje"));
        assertEquals("Diez letras", TextoModeracion.explicacion("  Diez letras  ", "mensaje"));
        assertEquals(1000, TextoModeracion.explicacion("x".repeat(1500), "mensaje").length());
        assertThrows(IllegalArgumentException.class, () -> TextoModeracion.requerido(" ", "mensaje", 100));
    }

    @Test
    void unBloqueoQuedaComoCasoCerradoDeGravedadAlta() {
        UserReport caso = UserReport.bloqueo(5L, 2L, "Acoso", "Mensajes ofensivos", "data:image/jpeg;base64,/9j/", AHORA);

        assertEquals("resolved", caso.getStatus());
        assertEquals("high", caso.getSeverity());
        assertEquals(2, caso.getReviewedByAdminId());
        assertEquals(AHORA, caso.getReviewedAt());
        assertTrue(caso.tieneEvidencia());
    }

    @Test
    void unReporteNecesitaMotivoDescripcionYGravedadValida() {
        assertEquals("medium", UserReport.reporte(5L, 2L, "Spam", "Enlaces", null).getSeverity());
        assertThrows(IllegalArgumentException.class, () -> UserReport.reporte(5L, 2L, " ", "Enlaces", null));
        assertThrows(IllegalArgumentException.class, () -> UserReport.reporte(5L, 2L, "Spam", "Enlaces", "enorme"));
    }

    @Test
    void cerrarUnCasoGuardaQuienYCuandoLoReviso() {
        UserReport r = UserReport.reporte(5L, 2L, "Spam", "Enlaces", "low");

        r.actualizar(new CambiosReporte(null, null, null, "in_review", null), 3L, AHORA);
        assertNull(r.getReviewedAt());
        r.actualizar(new CambiosReporte(null, null, "high", "resolved", " Advertida "), 3L, AHORA);
        assertEquals(3, r.getReviewedByAdminId());
        assertEquals(AHORA, r.getReviewedAt());
        assertEquals("Advertida", r.getResolutionNotes());
        assertThrows(IllegalArgumentException.class, () -> r.actualizar(new CambiosReporte(null, null, null, "raro", null), 3L, AHORA));
    }

    @Test
    void eliminarUnaCuentaNecesitaDosAdministradorasDistintas() {
        assertThrows(IllegalArgumentException.class, () -> AccountDeletionRequest.nueva(2L, 2L, "Me quiero borrar"));
        AccountDeletionRequest s = AccountDeletionRequest.nueva(5L, 2L, "Cuenta duplicada");

        assertThrows(IllegalArgumentException.class, () -> s.aprobar(2L, null, AHORA));
        s.aprobar(3L, "  Confirmado  ", AHORA);
        assertEquals("approved", s.getStatus());
        assertEquals("Confirmado", s.getDecisionNote());
        assertEquals(3L, s.getDecidedBy());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> s.rechazar(3L, null, AHORA));
        assertEquals("Esta solicitud ya fue decidida", e.getMessage());
    }

    @Test
    void quienPideLaEliminacionPuedeCancelarla() {
        AccountDeletionRequest s = AccountDeletionRequest.nueva(5L, 2L, "Cuenta duplicada");
        s.rechazar(2L, " ", AHORA);

        assertEquals("rejected", s.getStatus());
        assertNull(s.getDecisionNote());
    }
}

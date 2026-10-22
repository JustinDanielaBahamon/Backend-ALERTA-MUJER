package com.alertamujer.admin.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la moderación, sin Spring ni base de datos. */
class ModerationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void explanationsHaveAtLeastTenCharacters() {
        assertThrows(IllegalArgumentException.class, () -> ModerationText.explanation("corto", "mensaje"));
        assertThrows(IllegalArgumentException.class, () -> ModerationText.explanation(null, "mensaje"));
        assertEquals("Diez letras", ModerationText.explanation("  Diez letras  ", "mensaje"));
        assertEquals(1000, ModerationText.explanation("x".repeat(1500), "mensaje").length());
        assertThrows(IllegalArgumentException.class, () -> ModerationText.required(" ", "mensaje", 100));
    }

    @Test
    void aBlockStaysAsAClosedHighSeverityCase() {
        UserReport reportCase = UserReport.block(5L, 2L, "Acoso", "Mensajes ofensivos", "data:image/jpeg;base64,/9j/", NOW);

        assertEquals("resolved", reportCase.getStatus());
        assertEquals("high", reportCase.getSeverity());
        assertEquals(2, reportCase.getReviewedByAdminId());
        assertEquals(NOW, reportCase.getReviewedAt());
        assertTrue(reportCase.hasEvidence());
    }

    @Test
    void aReportNeedsAReasonDescriptionAndValidSeverity() {
        assertEquals("medium", UserReport.userReport(5L, 2L, "Spam", "Enlaces", null).getSeverity());
        assertThrows(IllegalArgumentException.class, () -> UserReport.userReport(5L, 2L, " ", "Enlaces", null));
        assertThrows(IllegalArgumentException.class, () -> UserReport.userReport(5L, 2L, "Spam", "Enlaces", "enorme"));
    }

    @Test
    void closingACaseRecordsWhoReviewedItAndWhen() {
        UserReport r = UserReport.userReport(5L, 2L, "Spam", "Enlaces", "low");

        r.update(new ReportChanges(null, null, null, "in_review", null), 3L, NOW);
        assertNull(r.getReviewedAt());
        r.update(new ReportChanges(null, null, "high", "resolved", " Advertida "), 3L, NOW);
        assertEquals(3, r.getReviewedByAdminId());
        assertEquals(NOW, r.getReviewedAt());
        assertEquals("Advertida", r.getResolutionNotes());
        assertThrows(IllegalArgumentException.class, () -> r.update(new ReportChanges(null, null, null, "raro", null), 3L, NOW));
    }

    @Test
    void deletingAnAccountNeedsTwoDifferentAdmins() {
        assertThrows(IllegalArgumentException.class, () -> AccountDeletionRequest.newOne(2L, 2L, "Me quiero borrar"));
        AccountDeletionRequest s = AccountDeletionRequest.newOne(5L, 2L, "Cuenta duplicada");

        assertThrows(IllegalArgumentException.class, () -> s.approve(2L, null, NOW));
        s.approve(3L, "  Confirmado  ", NOW);
        assertEquals("approved", s.getStatus());
        assertEquals("Confirmado", s.getDecisionNote());
        assertEquals(3L, s.getDecidedBy());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> s.reject(3L, null, NOW));
        assertEquals("Esta solicitud ya fue decidida", e.getMessage());
    }

    @Test
    void whoeverRequestsTheDeletionCanCancelIt() {
        AccountDeletionRequest s = AccountDeletionRequest.newOne(5L, 2L, "Cuenta duplicada");
        s.reject(2L, " ", NOW);

        assertEquals("rejected", s.getStatus());
        assertNull(s.getDecisionNote());
    }
}

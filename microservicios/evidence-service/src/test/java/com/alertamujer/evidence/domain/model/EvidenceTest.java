package com.alertamujer.evidence.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Domain rules: no Spring, no database, no mocks. */
class EvidenceTest {

    @Test
    void detectsTheTypeFromTheFileAndRejectsDangerousOnes() {
        assertEquals("photo", Evidence.typeOf("image/jpeg"));
        assertEquals("video", Evidence.typeOf("video/mp4"));
        assertEquals("audio", Evidence.typeOf("audio/mp4"));
        assertEquals("document", Evidence.typeOf("application/pdf"));
        assertThrows(IllegalArgumentException.class, () -> Evidence.typeOf("image/svg+xml"));
        assertThrows(IllegalArgumentException.class, () -> Evidence.typeOf("text/html"));
    }

    @Test
    void renameKeepsTheExtension() {
        Evidence e = new Evidence();
        e.setFileName("video_1760870000000.mp4");

        e.rename("  Camino a casa  ");

        assertEquals("Camino a casa.mp4", e.getFileName());
    }

    @Test
    void rejectsEmptyOrTooLongNames() {
        Evidence e = new Evidence();
        assertThrows(IllegalArgumentException.class, () -> e.rename("   "));
        assertThrows(IllegalArgumentException.class, () -> e.rename("a".repeat(81)));
    }

    @Test
    void onlyAcceptsKnownReviewStatuses() {
        Evidence e = new Evidence();
        e.changeReview("verified");
        assertEquals("verified", e.getReviewStatus());
        assertThrows(IllegalArgumentException.class, () -> e.changeReview("Verificada"));
    }
}

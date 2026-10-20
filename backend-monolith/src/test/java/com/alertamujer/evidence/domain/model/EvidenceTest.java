package com.alertamujer.evidence.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas del dominio: sin Spring, sin base de datos, sin mocks. */
class EvidenceTest {

    @Test
    void reconoceElTipoPorElArchivoYRechazaLosPeligrosos() {
        assertEquals("photo", Evidence.tipoDe("image/jpeg"));
        assertEquals("video", Evidence.tipoDe("video/mp4"));
        assertEquals("audio", Evidence.tipoDe("audio/mp4"));
        assertEquals("document", Evidence.tipoDe("application/pdf"));
        assertThrows(IllegalArgumentException.class, () -> Evidence.tipoDe("image/svg+xml"));
        assertThrows(IllegalArgumentException.class, () -> Evidence.tipoDe("text/html"));
    }

    @Test
    void renombrarConservaLaExtension() {
        Evidence e = new Evidence();
        e.setFileName("video_1760870000000.mp4");

        e.renombrar("  Camino a casa  ");

        assertEquals("Camino a casa.mp4", e.getFileName());
    }

    @Test
    void rechazaNombresVaciosOMuyLargos() {
        Evidence e = new Evidence();
        assertThrows(IllegalArgumentException.class, () -> e.renombrar("   "));
        assertThrows(IllegalArgumentException.class, () -> e.renombrar("a".repeat(81)));
    }

    @Test
    void soloAceptaEstadosDeRevisionConocidos() {
        Evidence e = new Evidence();
        e.cambiarRevision("verified");
        assertEquals("verified", e.getReviewStatus());
        assertThrows(IllegalArgumentException.class, () -> e.cambiarRevision("Verificada"));
    }
}

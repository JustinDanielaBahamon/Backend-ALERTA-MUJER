package com.alertamujer.resources.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de recursos y llamadas, sin Spring ni base de datos. */
class ResourceCallTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static EmergencyResource recurso(String telefono) {
        EmergencyResource r = EmergencyResource.nuevo(new DatosRecurso("Línea 155", "emergency_line", telefono, null, null,
                null, "Bogotá", BigDecimal.ONE, BigDecimal.ONE, null, null));
        r.setId(7L);
        return r;
    }

    @Test
    void unRecursoNuevoQuedaActivo() {
        assertTrue(recurso("155").getIsActive());
    }

    @Test
    void sinNumeroMarcadoUsaElDelRecursoYQuedaIniciada() {
        ResourceCall c = ResourceCall.nueva(10L, recurso("155"), null, null, " ", null, AHORA);

        assertEquals("155", c.getTelephoneDialed());
        assertEquals("initiated", c.getStatus());
        assertEquals(7L, c.getEmergencyResourceId());
        assertEquals(AHORA, c.getStartedAt());
    }

    @Test
    void respetaElNumeroYElEstadoQueEnviaLaApp() {
        ResourceCall c = ResourceCall.nueva(10L, recurso("155"), 3L, 4L, " 3001 ", "connected", AHORA);

        assertEquals("3001", c.getTelephoneDialed());
        assertEquals("connected", c.getStatus());
    }

    @Test
    void unRecursoSinTelefonoNoSePuedeLlamar() {
        assertThrows(IllegalArgumentException.class, () -> ResourceCall.nueva(10L, recurso(null), null, null, null, null, AHORA));
    }

    @Test
    void actualizarSoloCambiaLoQueLlega() {
        ResourceCall c = ResourceCall.nueva(10L, recurso("155"), null, null, null, null, AHORA);
        c.actualizar(null, null, 45);

        assertEquals("initiated", c.getStatus());
        assertEquals(45, c.getDurationSeconds());
        assertTrue(c.esDe(10L));
        assertFalse(c.esDe(20L));
    }
}

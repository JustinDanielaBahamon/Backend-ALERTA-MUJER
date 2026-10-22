package com.alertamujer.resources.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Resource and call rules, without Spring or a database. */
class ResourceCallTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static EmergencyResource resource(String telephone) {
        EmergencyResource r = EmergencyResource.created(new ResourceData("Línea 155", "emergency_line", telephone, null, null,
                null, "Bogotá", BigDecimal.ONE, BigDecimal.ONE, null, null));
        r.setId(7L);
        return r;
    }

    @Test
    void aNewResourceIsActive() {
        assertTrue(resource("155").getIsActive());
    }

    @Test
    void withoutDialedNumberUsesTheResourceOneAndIsStarted() {
        ResourceCall c = ResourceCall.newOne(10L, resource("155"), null, null, " ", null, NOW);

        assertEquals("155", c.getTelephoneDialed());
        assertEquals("initiated", c.getStatus());
        assertEquals(7L, c.getEmergencyResourceId());
        assertEquals(NOW, c.getStartedAt());
    }

    @Test
    void respectsTheNumberAndStatusTheAppSends() {
        ResourceCall c = ResourceCall.newOne(10L, resource("155"), 3L, 4L, " 3001 ", "connected", NOW);

        assertEquals("3001", c.getTelephoneDialed());
        assertEquals("connected", c.getStatus());
    }

    @Test
    void aResourceWithoutPhoneCannotBeCalled() {
        assertThrows(IllegalArgumentException.class, () -> ResourceCall.newOne(10L, resource(null), null, null, null, null, NOW));
    }

    @Test
    void updateOnlyChangesWhatArrives() {
        ResourceCall c = ResourceCall.newOne(10L, resource("155"), null, null, null, null, NOW);
        c.update(null, null, 45);

        assertEquals("initiated", c.getStatus());
        assertEquals(45, c.getDurationSeconds());
        assertTrue(c.esDe(10L));
        assertFalse(c.esDe(20L));
    }
}

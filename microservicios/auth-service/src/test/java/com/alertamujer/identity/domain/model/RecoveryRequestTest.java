package com.alertamujer.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RecoveryRequestTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void storesOnlyTheHashAndLastsFifteenMinutes() {
        RecoveryRequest r = RecoveryRequest.newOne(4L, "123456", "1.2.3.4", NOW);

        assertNotEquals("123456", r.getTokenHash());
        assertEquals(64, r.getTokenHash().length());
        assertTrue(r.coincide("123456"));
        assertFalse(r.coincide("654321"));
        assertFalse(r.expired(NOW.plusMinutes(14)));
        assertTrue(r.expired(NOW.plusMinutes(16)));
        assertEquals("pending", r.getStatus());
    }

    @Test
    void theSameCodeOnAnotherAccountGivesAnotherHash() {
        assertNotEquals(RecoveryRequest.hash(1L, "123456"), RecoveryRequest.hash(2L, "123456"));
    }

    @Test
    void theCodeStopsWorkingAfterTheFifthFailure() {
        RecoveryRequest r = RecoveryRequest.newOne(4L, "123456", null, NOW);
        for (int i = 0; i < RecoveryRequest.MAX_ATTEMPTS - 1; i++) r.recordFailure();
        assertEquals("pending", r.getStatus());

        r.recordFailure();
        assertEquals("expired", r.getStatus());
    }

    @Test
    void isRecentForOneMinute() {
        RecoveryRequest r = RecoveryRequest.newOne(4L, "123456", null, NOW);
        assertTrue(r.recent(NOW.plusSeconds(30)));
        assertFalse(r.recent(NOW.plusSeconds(61)));
    }
}

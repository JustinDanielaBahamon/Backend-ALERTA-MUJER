package com.alertamujer.notifications.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Tracking link rules, without Spring or a database. */
class TrackingLinkTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void theCodeIsRandomAndOnlyItsHashIsStored() {
        String code = TrackingLink.generateCode();

        assertEquals(43, code.length());
        assertNotEquals(code, TrackingLink.generateCode());
        TrackingLink link = TrackingLink.forAlert(5L, code);
        assertEquals(5L, link.getAlertId());
        assertEquals(TrackingLink.hash(code), link.getTokenHash());
        assertFalse(link.getTokenHash().contains(code));
        assertFalse(link.revoked());
    }

    @Test
    void discardsCodesThatCannotBeValid() {
        assertFalse(TrackingLink.possibleFormat(null));
        assertFalse(TrackingLink.possibleFormat("corto"));
        assertFalse(TrackingLink.possibleFormat("x".repeat(101)));
        assertTrue(TrackingLink.possibleFormat(TrackingLink.generateCode()));
    }

    @Test
    void trackingIsValidUpToTwoHoursAfterEnding() {
        assertTrue(TrackingLink.liveTracking("active", null, NOW));
        assertTrue(TrackingLink.liveTracking("resolved", NOW.minusMinutes(119), NOW));
        assertFalse(TrackingLink.liveTracking("resolved", NOW.minusMinutes(121), NOW));
        assertTrue(TrackingLink.liveTracking("cancelled", null, NOW));
    }
}

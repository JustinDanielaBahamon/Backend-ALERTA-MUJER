package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Alert rules, without Spring or a database. */
class AlertTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static Alert active() {
        Alert a = new Alert();
        a.setId(1L);
        a.setUserProfileId(10L);
        a.setStartedAt(NOW.minusMinutes(5));
        return a;
    }

    private static AlertChanges status(String status) {
        return new AlertChanges(null, null, status, null, null, null);
    }

    @Test
    void aNewAlertIsActiveAndPrimary() {
        Alert a = new Alert();
        assertTrue(a.isActive());
        assertEquals("main", a.getAlertType());
        assertFalse(a.isKeepEvidence());
        assertFalse(a.ended());
    }

    @Test
    void belongsOnlyToItsOwner() {
        Alert a = active();
        assertTrue(a.esDe(10L));
        assertFalse(a.esDe(20L));
        assertFalse(a.esDe(null));
    }

    @Test
    void cancellingAnActiveAlertSavesTheTimeAndNotifiesItEnded() {
        Alert a = active();

        assertTrue(a.update(status("cancelled"), NOW));

        assertEquals("cancelled", a.getStatus());
        assertEquals(NOW, a.getCancelledAt());
        assertNull(a.getEndedAt());
        assertTrue(a.ended());
    }

    @Test
    void resolveRespectsTheTimeTheAppSent() {
        Alert a = active();
        LocalDateTime sent = NOW.minusMinutes(1);

        assertTrue(a.update(new AlertChanges(null, null, "resolved", null, sent, null), NOW));

        assertEquals(sent, a.getEndedAt());
    }

    @Test
    void changesWithoutEndingItDoNotNotifyAndOnlyTouchWhatWasSent() {
        Alert a = active();
        a.setMessage("Ayuda");

        assertFalse(a.update(new AlertChanges("silent", null, null, null, null, null), NOW));

        assertEquals("silent", a.getAlertType());
        assertEquals("Ayuda", a.getMessage());
        assertTrue(a.isActive());
    }

    @Test
    void endingAnAlreadyEndedAlertDoesNotNotifyAgain() {
        Alert a = active();
        a.update(status("cancelled"), NOW);

        assertFalse(a.update(status("resolved"), NOW.plusMinutes(1)));
    }

    @Test
    void theAdministrationOnlyAssignsKnownStatuses() {
        Alert a = active();

        assertThrows(IllegalArgumentException.class, () -> a.changeStatus("borrada", NOW));
        assertThrows(IllegalArgumentException.class, () -> a.changeStatus(null, NOW));
        assertTrue(a.changeStatus("resolved", NOW));
        assertEquals(NOW, a.getEndedAt());
        assertFalse(a.changeStatus("cancelled", NOW));
        assertEquals(NOW, a.getCancelledAt());
    }

    @Test
    void checkInDeadlineAndAbsoluteLimit() {
        Alert a = active(); // started 5 min ago
        Duration twoHours = Duration.ofHours(2);

        assertEquals(NOW.plusMinutes(25), a.deadline(30));
        assertFalse(a.unanswered(NOW, 30));
        assertTrue(a.unanswered(NOW.plusMinutes(25), 30));
        assertFalse(a.overHardLimit(NOW.plusMinutes(114), twoHours));
        assertTrue(a.overHardLimit(NOW.plusMinutes(115), twoHours));

        a.expire(NOW);
        assertEquals("expired", a.getStatus());
        assertFalse(a.unanswered(NOW.plusHours(1), 30));
    }

    @Test
    void stillInDangerAddsTheCheckInMinutesWithoutPassingTheLimit() {
        Alert a = active();
        Duration twoHours = Duration.ofHours(2);
        a.markUnanswered(NOW);

        a.extend(NOW, 30, twoHours); // before the deadline: counted from it
        assertEquals(NOW.plusMinutes(55), a.getExtendedUntil());
        assertNull(a.getUnansweredAt());

        a.extend(NOW.plusMinutes(100), 30, twoHours); // late: from now, capped at the limit
        assertEquals(NOW.plusMinutes(115), a.getExtendedUntil());
    }

    @Test
    void duressIsMarkedOnceAndOnlyOnAnActiveAlert() {
        Alert a = active();
        assertTrue(a.markDuress(NOW));
        assertFalse(a.markDuress(NOW.plusMinutes(1)));
        assertEquals(NOW, a.getDuressAt());

        a.expire(NOW);
        assertThrows(IllegalArgumentException.class, () -> a.extend(NOW, 30, Duration.ofHours(2)));
    }

    @Test
    void keepsEvidenceOfUpTo5Alerts() {
        Alert a = active();

        a.keepEvidence(true, 4);
        assertTrue(a.isKeepEvidence());

        // Already kept: marking it again does not count as one more
        a.keepEvidence(true, Alert.MAX_KEPT_ALERTS);
        a.keepEvidence(false, Alert.MAX_KEPT_ALERTS);
        assertFalse(a.isKeepEvidence());

        assertThrows(IllegalArgumentException.class, () -> a.keepEvidence(true, Alert.MAX_KEPT_ALERTS));
        assertFalse(a.isKeepEvidence());
    }
}

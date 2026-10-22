package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlertContactTest {

    @Test
    void aNewDeliveryIsPending() {
        AlertContact d = AlertContact.created(5L, 1L, AlertContact.EMAIL_CHANNEL, "eva@correo.com");

        assertEquals("pending", d.getStatus());
        assertEquals(0, d.getAttempts());
    }

    @Test
    void recordsEachAttemptAndTruncatesTheError() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 20, 10, 0);
        AlertContact d = AlertContact.created(5L, 1L, AlertContact.EMAIL_CHANNEL, "eva@correo.com");

        d.recordAttempt("failed", "x".repeat(800), now);
        assertEquals(1, d.getAttempts());
        assertEquals(500, d.getErrorMessage().length());
        assertNull(d.getSentAt());

        d.recordAttempt("sent", null, now);
        assertEquals(2, d.getAttempts());
        assertEquals(now, d.getSentAt());
        assertNull(d.getErrorMessage());
    }

    @Test
    void anOpenDeliveryOfAnEndedAlertLooksExpired() {
        AlertContact pending = AlertContact.created(5L, 1L, AlertContact.PUSH_CHANNEL, "App");
        AlertContact failed = AlertContact.created(5L, 1L, AlertContact.EMAIL_CHANNEL, "eva@correo.com");
        failed.setStatus("failed");

        assertEquals("pending", pending.visibleStatus(false));
        assertEquals("expired", pending.visibleStatus(true));
        assertEquals("failed", failed.visibleStatus(true));
    }
}

package com.alertamujer.notifications.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContactMessageLimitTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Test
    void oneMessageEveryTwoSecondsPerLink() {
        ContactMessageLimit limit = new ContactMessageLimit();

        limit.registrar("enlace", NOW);
        assertThrows(IllegalArgumentException.class, () -> limit.registrar("enlace", NOW.plusSeconds(1)));
        assertDoesNotThrow(() -> limit.registrar("otro", NOW.plusSeconds(1)));
        assertDoesNotThrow(() -> limit.registrar("enlace", NOW.plusSeconds(3)));
    }

    @Test
    void atMost30EveryTenMinutes() {
        ContactMessageLimit limit = new ContactMessageLimit();
        for (int i = 0; i < ContactMessageLimit.MAX_IN_WINDOW; i++) limit.registrar("enlace", NOW.plusSeconds(3L * i));

        assertThrows(IllegalArgumentException.class, () -> limit.registrar("enlace", NOW.plusSeconds(100)));
        assertDoesNotThrow(() -> limit.registrar("enlace", NOW.plusMinutes(11)));
    }
}

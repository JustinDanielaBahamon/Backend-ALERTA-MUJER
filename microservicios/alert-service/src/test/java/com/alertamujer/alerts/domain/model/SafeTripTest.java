package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SafeTripTest {

    private static final LocalDateTime DEPARTURE = LocalDateTime.of(2026, 10, 21, 20, 0);

    private SafeTrip trip(int minutes) {
        return SafeTrip.created(10L, 1L, 2L, "  Casa  ", minutes, null, null, DEPARTURE);
    }

    @Test
    void create_computesTheArrivalTimeAndValidatesTheLimits() {
        SafeTrip t = trip(40);
        assertEquals("Casa", t.getDestination());
        assertEquals(DEPARTURE.plusMinutes(40), t.getExpectedArrival());
        assertTrue(t.active());
        assertThrows(IllegalArgumentException.class, () -> trip(4));
        assertThrows(IllegalArgumentException.class, () -> trip(241));
        assertThrows(IllegalArgumentException.class, () -> SafeTrip.created(10L, 1L, 2L, " ", 30, null, null, DEPARTURE));
    }

    @Test
    void asksFiveMinutesBeforeAndExpiresWithTwoMinutesGrace() {
        SafeTrip t = trip(40);
        assertFalse(t.dueSoon(DEPARTURE.plusMinutes(34)));
        assertTrue(t.dueSoon(DEPARTURE.plusMinutes(35)));
        t.markReminder();
        assertFalse(t.dueSoon(DEPARTURE.plusMinutes(36)), "solo se pregunta una vez");
        assertFalse(t.expired(DEPARTURE.plusMinutes(42)));
        assertTrue(t.expired(DEPARTURE.plusMinutes(42).plusSeconds(1)));
    }

    @Test
    void extendMovesTheTimeAndAsksAgain() {
        SafeTrip t = trip(40);
        t.markReminder();
        t.extender(15);
        assertEquals(DEPARTURE.plusMinutes(55), t.getExpectedArrival());
        assertTrue(t.dueSoon(DEPARTURE.plusMinutes(50)));
        assertThrows(IllegalArgumentException.class, () -> t.extender(61));
    }

    @Test
    void cannotLastMoreThanSixHours() {
        SafeTrip t = trip(240);
        t.extender(60);
        t.extender(60);
        assertThrows(IllegalArgumentException.class, () -> t.extender(1));
    }

    @Test
    void anEndedTripIsNotReused() {
        SafeTrip t = trip(30);
        t.arrive(DEPARTURE.plusMinutes(20));
        assertFalse(t.active());
        assertFalse(t.expired(DEPARTURE.plusHours(5)));
        assertThrows(IllegalArgumentException.class, () -> t.extender(10));
        assertThrows(IllegalArgumentException.class, () -> t.cancel(DEPARTURE));
    }
}

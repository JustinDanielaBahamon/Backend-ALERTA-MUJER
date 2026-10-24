package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
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

    @Test
    void withoutSignalsOfLifeTheSignalIsNeverLost() {
        SafeTrip t = trip(60);
        assertFalse(t.signalLost(DEPARTURE.plusMinutes(50), SafeTrip.SIGNAL_TIMEOUT),
                "una app que no envía señales no da falsas alarmas");
    }

    @Test
    void signalIsLostWhenTheSignalsStopBeforeTheArrival() {
        SafeTrip t = trip(60);
        t.heartbeat(new BigDecimal("2.93"), new BigDecimal("-75.28"), DEPARTURE.plusMinutes(10));
        assertFalse(t.signalLost(DEPARTURE.plusMinutes(25), SafeTrip.SIGNAL_TIMEOUT));
        assertTrue(t.signalLost(DEPARTURE.plusMinutes(25).plusSeconds(1), SafeTrip.SIGNAL_TIMEOUT));
        assertTrue(t.signalLost(DEPARTURE.plusMinutes(16), SafeTrip.SIGNAL_TIMEOUT_RISK_ZONE),
                "en zona de riesgo espera menos");
        assertEquals(new BigDecimal("2.93"), t.getLastLatitude());
    }

    @Test
    void afterTheArrivalTimeItIsNotConfirmedInsteadOfSignalLost() {
        SafeTrip t = trip(20);
        t.heartbeat(null, null, DEPARTURE.plusMinutes(1));
        assertTrue(t.expired(DEPARTURE.plusMinutes(23)));
        assertFalse(t.signalLost(DEPARTURE.plusMinutes(23), Duration.ofMinutes(5)));
    }

    @Test
    void aHeartbeatWithoutLocationKeepsTheLastOne() {
        SafeTrip t = trip(60);
        t.heartbeat(new BigDecimal("2.93"), new BigDecimal("-75.28"), DEPARTURE.plusMinutes(2));
        t.heartbeat(null, null, DEPARTURE.plusMinutes(4));
        assertEquals(DEPARTURE.plusMinutes(4), t.getLastSeenAt());
        assertEquals(new BigDecimal("-75.28"), t.getLastLongitude());
    }

    @Test
    void anEndedTripRejectsHeartbeatsAndRecordsWhyItWasAlerted() {
        SafeTrip t = trip(30);
        t.markAlerted(7L, DEPARTURE.plusMinutes(20), SafeTrip.REASON_SIGNAL_LOST);
        assertEquals(SafeTrip.REASON_SIGNAL_LOST, t.getAlertReason());
        assertThrows(IllegalArgumentException.class, () -> t.heartbeat(null, null, DEPARTURE.plusMinutes(21)));
    }
}

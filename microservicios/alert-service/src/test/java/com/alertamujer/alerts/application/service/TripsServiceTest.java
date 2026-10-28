package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.AlertCheckInUseCase;
import com.alertamujer.alerts.application.port.in.ManageOwnAlertsUseCase;
import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.application.port.in.TripsUseCase.Heartbeat;
import com.alertamujer.alerts.application.port.in.TripsUseCase.NewTrip;
import com.alertamujer.alerts.application.port.out.*;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.alerts.domain.model.SafeTrip;
import com.alertamujer.shared.exception.ConflictException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripsServiceTest {

    @Mock private TripRepository trips;
    @Mock private IdentityPort identity;
    @Mock private ManageOwnAlertsUseCase alerts;
    @Mock private LocationRepository locations;
    @Mock private PhonesPort phones;
    @Mock private PushPort push;
    @Mock private RiskZonesPort riskZones;
    @Mock private ProtectionDeviceRepository protectedPhones;
    @Mock private AlertCheckInUseCase checkIn;

    @InjectMocks private TripsService service;

    private static final LocalDateTime DEPARTURE = LocalDateTime.of(2026, 10, 21, 20, 0);

    @BeforeEach
    void setUp() {
        lenient().when(identity.requiredProfile(1L)).thenReturn(10L);
        lenient().when(trips.save(any(SafeTrip.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static SafeTrip trip(int minutes) {
        SafeTrip t = SafeTrip.created(10L, 1L, 2L, "Casa", minutes, null, null, DEPARTURE);
        t.setId(7L);
        return t;
    }

    @Test
    void start_onlyOneAtATime() {
        when(trips.activeOf(10L)).thenReturn(Optional.of(trip(30)));

        assertThrows(ConflictException.class, () -> service.start(new NewTrip("Casa", 30, 2L, null, null), 1L));
        verify(trips, never()).save(any());
    }

    @Test
    void start_withDailyLimit() {
        when(trips.activeOf(10L)).thenReturn(Optional.empty());
        when(trips.startedSince(eq(10L), any())).thenReturn((long) TripsService.MAX_PER_DAY);

        assertThrows(IllegalArgumentException.class, () -> service.start(new NewTrip("Casa", 30, 2L, null, null), 1L));
    }

    @Test
    void onlyTheOwnerConfirmsOrCancels() {
        when(trips.find(7L)).thenReturn(Optional.of(trip(30)));
        when(identity.requiredProfile(99L)).thenReturn(99L);

        assertThrows(ResourceNotFoundException.class, () -> service.arrived(7L, 99L));
        assertEquals(SafeTrip.ARRIVED, service.arrived(7L, 1L).getStatus());
    }

    @Test
    void review_alertsWithTheLastLocationWhenNotConfirmed() {
        SafeTrip t = trip(30);
        when(trips.allActive()).thenReturn(List.of(t));
        LocationLog latest = new LocationLog();
        latest.setLatitude(new BigDecimal("2.93"));
        latest.setLongitude(new BigDecimal("-75.28"));
        latest.setRecordedAt(DEPARTURE.plusMinutes(25));
        when(locations.latest(10L)).thenReturn(Optional.of(latest));
        Alert created = new Alert();
        created.setId(55L);
        when(alerts.activate(any(NewAlert.class), eq(1L))).thenReturn(created);

        assertEquals(1, service.review(DEPARTURE.plusMinutes(33)));

        ArgumentCaptor<NewAlert> newOne = ArgumentCaptor.forClass(NewAlert.class);
        verify(alerts).activate(newOne.capture(), eq(1L));
        assertEquals("timer", newOne.getValue().activationMethod());
        assertEquals(new BigDecimal("2.93"), newOne.getValue().latitude());
        assertTrue(newOne.getValue().message().contains("Casa"));
        assertEquals(SafeTrip.ALERTED, t.getStatus());
        assertEquals(55L, t.getAlertId());
    }

    @Test
    void review_asksIfArrivedBeforeTheTime() {
        SafeTrip t = trip(30);
        when(trips.allActive()).thenReturn(List.of(t));
        when(phones.phoneTokens(1L)).thenReturn(List.of("token"));

        assertEquals(0, service.review(DEPARTURE.plusMinutes(26)));

        verify(push).sendData(eq(List.of("token")), argThat((Map<String, String> m) -> "TRAYECTO_POR_VENCER".equals(m.get("tipo"))));
        assertTrue(t.isReminderSent());
        verify(alerts, never()).activate(any(), any());
    }

    @Test
    void review_oneErrorDoesNotStopTheOthers() {
        SafeTrip bad = trip(30);
        SafeTrip good = trip(30);
        good.setId(8L);
        when(trips.allActive()).thenReturn(List.of(bad, good));
        when(locations.latest(10L)).thenReturn(Optional.empty());
        Alert created = new Alert();
        created.setId(56L);
        when(alerts.activate(any(), eq(1L))).thenThrow(new RuntimeException("caída")).thenReturn(created);

        assertEquals(1, service.review(DEPARTURE.plusMinutes(40)));
        assertEquals(SafeTrip.ALERTED, good.getStatus());
    }

    @Test
    void review_alertsWhenTheSignalOfLifeStopsBeforeTheArrival() {
        SafeTrip t = trip(60);
        t.heartbeat(new BigDecimal("2.94"), new BigDecimal("-75.29"), DEPARTURE.plusMinutes(10));
        when(trips.allActive()).thenReturn(List.of(t));
        when(locations.latest(10L)).thenReturn(Optional.empty());
        Alert created = new Alert();
        created.setId(57L);
        when(alerts.activate(any(NewAlert.class), eq(1L))).thenReturn(created);

        assertEquals(0, service.review(DEPARTURE.plusMinutes(24)), "aún dentro de los 15 min");
        assertEquals(1, service.review(DEPARTURE.plusMinutes(26)));

        ArgumentCaptor<NewAlert> newOne = ArgumentCaptor.forClass(NewAlert.class);
        verify(alerts).activate(newOne.capture(), eq(1L));
        assertEquals("signal_lost", newOne.getValue().activationMethod());
        assertEquals(new BigDecimal("2.94"), newOne.getValue().latitude(), "usa la última señal de vida");
        assertTrue(newOne.getValue().message().contains("Se perdió la señal"));
        assertEquals(SafeTrip.REASON_SIGNAL_LOST, t.getAlertReason());
    }

    @Test
    void review_waitsLessInsideAHighRiskZone() {
        SafeTrip t = trip(60);
        t.heartbeat(new BigDecimal("2.94"), new BigDecimal("-75.29"), DEPARTURE.plusMinutes(10));
        when(trips.allActive()).thenReturn(List.of(t));
        when(riskZones.isHighRisk(new BigDecimal("2.94"), new BigDecimal("-75.29"))).thenReturn(true);
        when(locations.latest(10L)).thenReturn(Optional.empty());
        Alert created = new Alert();
        created.setId(58L);
        when(alerts.activate(any(NewAlert.class), eq(1L))).thenReturn(created);

        assertEquals(1, service.review(DEPARTURE.plusMinutes(16)));
    }

    @Test
    void heartbeat_updatesTheTripAndStoresTheRoutePoint() {
        SafeTrip t = trip(60);
        when(trips.find(7L)).thenReturn(Optional.of(t));

        service.heartbeat(7L, new Heartbeat(new BigDecimal("2.95"), new BigDecimal("-75.3"), null, null), 1L);

        assertNotNull(t.getLastSeenAt());
        ArgumentCaptor<LocationLog> point = ArgumentCaptor.forClass(LocationLog.class);
        verify(locations).save(point.capture());
        assertNull(point.getValue().getAlertId());
    }

    @Test
    void heartbeat_afterTheAlertAddsTheOfflineRouteToIt() {
        SafeTrip t = trip(60);
        t.markAlerted(59L, DEPARTURE.plusMinutes(30), SafeTrip.REASON_SIGNAL_LOST);
        when(trips.find(7L)).thenReturn(Optional.of(t));

        service.heartbeat(7L, new Heartbeat(new BigDecimal("2.95"), new BigDecimal("-75.3"), null,
                LocalDateTime.now().minusMinutes(5)), 1L);

        ArgumentCaptor<LocationLog> point = ArgumentCaptor.forClass(LocationLog.class);
        verify(locations).save(point.capture());
        assertEquals(59L, point.getValue().getAlertId());
        verify(trips, never()).save(any());
    }

    @Test
    void timesInTheMessageAreInSpanish() {
        String text = TripsService.TIME.format(java.time.LocalTime.of(22, 5));
        assertTrue(text.startsWith("10:05"), text);
        assertFalse(text.contains("PM"), text);
        assertTrue(text.contains("p"), text);
    }
}

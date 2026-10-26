package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.AutoProtectionUseCase.AutoHeartbeat;
import com.alertamujer.alerts.application.port.in.ManageOwnAlertsUseCase;
import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.application.port.in.TripsUseCase.NewTrip;
import com.alertamujer.alerts.application.port.out.*;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.ProtectionDevice;
import com.alertamujer.alerts.domain.model.SafeTrip;
import com.alertamujer.shared.exception.AccessDeniedException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoProtectionTest {

    @Mock private TripRepository trips;
    @Mock private IdentityPort identity;
    @Mock private ManageOwnAlertsUseCase alerts;
    @Mock private LocationRepository locations;
    @Mock private PhonesPort phones;
    @Mock private PushPort push;
    @Mock private RiskZonesPort riskZones;
    @Mock private ProtectionDeviceRepository protectedPhones;

    @InjectMocks private TripsService service;

    private static final String KEY = "llave-de-prueba";
    private static final LocalDateTime OUT = LocalDateTime.of(2026, 10, 25, 18, 0);

    @BeforeEach
    void setUp() {
        lenient().when(trips.save(any(SafeTrip.class))).thenAnswer(inv -> inv.getArgument(0));
        ProtectionDevice phone = new ProtectionDevice();
        phone.setUserProfileId(10L);
        phone.setUserId(1L);
        phone.setDeviceId(2L);
        lenient().when(protectedPhones.byKeyHash(ProtectionDevice.hash(KEY))).thenReturn(Optional.of(phone));
    }

    private static SafeTrip auto() {
        SafeTrip t = SafeTrip.automatic(10L, 1L, 2L, new BigDecimal("2.93"), new BigDecimal("-75.28"), OUT);
        t.setId(5L);
        return t;
    }

    @Test
    void enable_givesANewKeyAndStoresOnlyItsHash() {
        when(identity.requiredProfile(1L)).thenReturn(10L);
        ArgumentCaptor<ProtectionDevice> saved = ArgumentCaptor.forClass(ProtectionDevice.class);

        String key = service.enable(1L, 2L);

        verify(protectedPhones).deleteOf(10L, 2L);
        verify(protectedPhones).save(saved.capture());
        assertTrue(key.length() >= 43);
        assertEquals(ProtectionDevice.hash(key), saved.getValue().getKeyHash());
        assertNotEquals(key, saved.getValue().getKeyHash());
    }

    @Test
    void anUnknownKeyCannotDoAnything() {
        when(protectedPhones.byKeyHash(any())).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.startAuto("otra", null, null));
        assertThrows(AccessDeniedException.class, () -> service.startAuto(null, null, null));
        verify(trips, never()).save(any());
    }

    @Test
    void start_reusesTheTripOrProtectionAlreadyGoingOn() {
        SafeTrip going = SafeTrip.created(10L, 1L, 2L, "Casa", 30, null, null, OUT);
        when(trips.activeOf(10L)).thenReturn(Optional.of(going));

        assertSame(going, service.startAuto(KEY, null, null));
        verify(trips, never()).save(any());
    }

    @Test
    void start_createsAProtectionWithoutDestination() {
        when(trips.activeOf(10L)).thenReturn(Optional.empty());

        SafeTrip t = service.startAuto(KEY, new BigDecimal("2.93"), new BigDecimal("-75.28"));

        assertTrue(t.automatic());
        assertEquals(SafeTrip.AUTO_DESTINATION, t.getDestination());
        assertNotNull(t.getLastSeenAt());
    }

    @Test
    void aTripSheStartsReplacesTheAutomaticProtection() {
        SafeTrip protection = auto();
        when(identity.requiredProfile(1L)).thenReturn(10L);
        when(trips.activeOf(10L)).thenReturn(Optional.of(protection));

        SafeTrip trip = service.start(new NewTrip("Universidad", 30, 2L, null, null), 1L);

        assertEquals(SafeTrip.ENDED, protection.getStatus());
        assertFalse(trip.automatic());
    }

    @Test
    void anotherPersonsProtectionIsRejected() {
        SafeTrip hers = SafeTrip.automatic(99L, 9L, 3L, null, null, OUT);
        hers.setId(8L);
        when(trips.find(8L)).thenReturn(Optional.of(hers));

        assertThrows(AccessDeniedException.class, () -> service.endAuto(KEY, 8L));
    }

    @Test
    void review_endsTheWindowWithoutAlerting() {
        SafeTrip t = auto();
        when(trips.allActive()).thenReturn(List.of(t));

        service.review(OUT.plus(SafeTrip.AUTO_WINDOW));

        assertEquals(SafeTrip.ENDED, t.getStatus());
        verify(alerts, never()).activate(any(), any());
    }

    @Test
    void review_alertsWhenTheSignalStopsAndExplainsTheBattery() {
        SafeTrip t = auto();
        t.heartbeat(null, null, true, OUT.plusMinutes(10));
        when(trips.allActive()).thenReturn(List.of(t));
        Alert created = new Alert();
        created.setId(50L);
        when(alerts.activate(any(), eq(1L))).thenReturn(created);
        ArgumentCaptor<NewAlert> alert = ArgumentCaptor.forClass(NewAlert.class);

        // 20 min without signal is not enough to alert in an automatic protection...
        service.review(OUT.plusMinutes(29));
        verify(alerts, never()).activate(any(), any());

        // ...but after the wait it alerts
        service.review(OUT.plusMinutes(31));
        verify(alerts).activate(alert.capture(), eq(1L));
        assertEquals("signal_lost", alert.getValue().activationMethod());
        assertTrue(alert.getValue().message().contains("batería"));
        assertEquals(SafeTrip.ALERTED, t.getStatus());
    }

    @Test
    void noAnswerInARiskZoneAlerts() {
        SafeTrip t = auto();
        when(trips.find(5L)).thenReturn(Optional.of(t));
        Alert created = new Alert();
        created.setId(51L);
        when(alerts.activate(any(), eq(1L))).thenReturn(created);
        ArgumentCaptor<NewAlert> alert = ArgumentCaptor.forClass(NewAlert.class);

        service.noAnswer(KEY, 5L);

        verify(alerts).activate(alert.capture(), eq(1L));
        assertEquals("auto", alert.getValue().activationMethod());
        assertTrue(alert.getValue().message().contains("no respondió"));
        assertEquals(SafeTrip.REASON_NO_ANSWER, t.getAlertReason());
    }

    @Test
    void herOwnPhonesAreToldTheAlertStarted() {
        SafeTrip t = auto();
        when(trips.find(5L)).thenReturn(Optional.of(t));
        Alert created = new Alert();
        created.setId(52L);
        when(alerts.activate(any(), eq(1L))).thenReturn(created);
        when(phones.phoneTokens(1L)).thenReturn(List.of("su-celular"));

        service.noAnswer(KEY, 5L);

        verify(push).sendData(eq(List.of("su-celular")), argThat((java.util.Map<String, String> m) ->
                "ALERTA_PROPIA".equals(m.get("tipo")) && "52".equals(m.get("alertaId"))));
    }

    @Test
    void notConfirmingSheArrivedWellAlerts() {
        SafeTrip t = auto();
        when(trips.find(5L)).thenReturn(Optional.of(t));
        Alert created = new Alert();
        created.setId(53L);
        when(alerts.activate(any(), eq(1L))).thenReturn(created);
        ArgumentCaptor<NewAlert> alert = ArgumentCaptor.forClass(NewAlert.class);

        service.unconfirmed(KEY, 5L);

        verify(alerts).activate(alert.capture(), eq(1L));
        assertTrue(alert.getValue().message().contains("no confirmó que llegó bien"));
        assertEquals(SafeTrip.REASON_UNCONFIRMED, t.getAlertReason());
    }

    @Test
    void heartbeat_keepsTheProtectionAlive() {
        SafeTrip t = auto();
        when(trips.find(5L)).thenReturn(Optional.of(t));

        service.autoHeartbeat(KEY, 5L, new AutoHeartbeat(new BigDecimal("2.94"), new BigDecimal("-75.29"),
                BigDecimal.TEN, null, false));

        assertEquals(new BigDecimal("2.94"), t.getLastLatitude());
        verify(locations).save(any());
    }
}

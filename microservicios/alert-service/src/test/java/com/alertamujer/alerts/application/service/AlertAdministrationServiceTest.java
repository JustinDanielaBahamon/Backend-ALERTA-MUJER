package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ManageAlertsUseCase.AdminDetail;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase.DeliveryChannel;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase.NotifiedContact;
import com.alertamujer.alerts.application.port.out.LocationRepository;
import com.alertamujer.alerts.domain.model.LocationLog;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertAdministrationServiceTest {

    @Mock private AlertRepository repository;
    @Mock private IdentityPort identity;
    @Mock private AlertEventsPort events;
    @Mock private LocationRepository locations;
    @Mock private AlertRecipientsUseCase recipients;

    @InjectMocks private AlertAdministrationService service;

    private static Alert alert(Long id, Long profile, LocalDateTime start) {
        Alert a = new Alert();
        a.setId(id);
        a.setUserProfileId(profile);
        a.setStartedAt(start);
        return a;
    }

    @Test
    void list_addsTheUserAndSortsNewestFirst() {
        LocalDateTime now = LocalDateTime.now();
        when(repository.all()).thenReturn(List.of(alert(1L, 10L, now.minusDays(1)), alert(2L, 20L, now)));
        when(identity.usersByProfile()).thenReturn(Map.of(10L, new UserSummary(1L, "Ana", "Gómez", "ana@test.com", null, 1L, null)));

        List<AdminAlertView> views = service.listAll();

        assertEquals(2L, views.get(0).id());
        assertNull(views.get(0).userName());
        assertEquals("Ana Gómez", views.get(1).userName());
        assertEquals("ana@test.com", views.get(1).userEmail());
        assertEquals(1L, views.get(1).userId());
    }

    @Test
    void changeStatus_endingAnActiveAlertNotifiesTheContacts() {
        Alert active = alert(1L, 10L, LocalDateTime.now());
        when(repository.find(1L)).thenReturn(Optional.of(active));
        when(repository.save(active)).thenReturn(active);

        Alert result = service.changeStatus(1L, "resolved");

        assertEquals("resolved", result.getStatus());
        assertNotNull(result.getEndedAt());
        verify(events).alertEnded(eq(active), any(LocalDateTime.class));
    }

    @Test
    void changeStatus_rejectsUnknownStatusesAndMissingAlerts() {
        when(repository.find(1L)).thenReturn(Optional.of(alert(1L, 10L, LocalDateTime.now())));
        when(repository.find(9L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(1L, "borrada"));
        assertThrows(ResourceNotFoundException.class, () -> service.changeStatus(9L, "resolved"));
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    private static LocationLog dot(int minute, double lat) {
        LocationLog l = new LocationLog();
        l.setLatitude(java.math.BigDecimal.valueOf(lat));
        l.setLongitude(java.math.BigDecimal.valueOf(-75.28));
        l.setRecordedAt(LocalDateTime.of(2026, 10, 20, 10, minute));
        return l;
    }

    @Test
    void detail_givesTheLastLocationAndDeliveriesWithoutNameOrEmail() {
        Alert alert = new Alert();
        alert.setId(5L);
        alert.setStatus("active");
        when(repository.find(5L)).thenReturn(Optional.of(alert));
        when(locations.ofAlert(5L)).thenReturn(List.of(dot(1, 2.91), dot(7, 2.93), dot(3, 2.92)));
        when(recipients.contactsOf(alert)).thenReturn(List.of(new NotifiedContact(9L, "Eva Ruiz", "Madre",
                List.of(new DeliveryChannel("email", "eva@correo.com", "sent", LocalDateTime.of(2026, 10, 20, 10, 0), null)))));

        AdminDetail d = service.detail(5L);

        assertTrue(d.locationVisible());
        assertEquals(3, d.points());
        assertEquals(LocalDateTime.of(2026, 10, 20, 10, 7), d.lastLocation().recordedAt());
        assertEquals(1, d.deliveries().get(0).contact());
        assertEquals("Madre", d.deliveries().get(0).relationship());
        assertEquals("sent", d.deliveries().get(0).channels().get(0).status());
        assertFalse(d.toString().contains("eva@correo.com"));
        assertFalse(d.toString().contains("Eva Ruiz"));
    }

    @Test
    void detail_ofACancelledAlertShowsNoLocations() {
        Alert alert = new Alert();
        alert.setId(6L);
        alert.setStatus("cancelled");
        when(repository.find(6L)).thenReturn(Optional.of(alert));
        when(recipients.contactsOf(alert)).thenReturn(List.of());

        AdminDetail d = service.detail(6L);

        assertFalse(d.locationVisible());
        assertNull(d.lastLocation());
        verify(locations, never()).ofAlert(any());
    }
}

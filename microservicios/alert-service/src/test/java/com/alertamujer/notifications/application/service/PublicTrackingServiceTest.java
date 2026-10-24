package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase.TrackingView;
import com.alertamujer.notifications.application.port.out.*;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublicTrackingServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);
    private static final String CODE = TrackingLink.generateCode();

    private final LinkRepository links = mock(LinkRepository.class);
    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final SettingsPort settings = mock(SettingsPort.class);
    private final EvidencePort evidences = mock(EvidencePort.class);
    private final ViewsRepository views = mock(ViewsRepository.class);
    private final AlertSignalRepository signals = mock(AlertSignalRepository.class);
    private final PublicTrackingService service = new PublicTrackingService(links, alerts, identity, settings, evidences, views,
            signals);

    private TrackingLink link;

    @BeforeEach
    void setUp() {
        link = TrackingLink.forAlert(5L, CODE);
        when(links.byHash(TrackingLink.hash(CODE))).thenReturn(Optional.of(link));
        when(identity.profileFirstName(10L)).thenReturn(Optional.of("Ana"));
        when(settings.emergencyNumber()).thenReturn("123");
    }

    private void alert(String status, LocalDateTime end) {
        when(alerts.find(5L)).thenReturn(Optional.of(new AlertSummary(5L, 10L, "main", "panic_button", status, null,
                NOW.minusHours(1), new BigDecimal("4.6"), new BigDecimal("-74.1"), end, null)));
    }

    @Test
    void createStoresOnlyTheCodeHash() {
        String code = service.createLink(5L);

        ArgumentCaptor<TrackingLink> stored = ArgumentCaptor.forClass(TrackingLink.class);
        verify(links).save(stored.capture());
        assertEquals(TrackingLink.hash(code), stored.getValue().getTokenHash());
        assertEquals(5L, stored.getValue().getAlertId());
    }

    @Test
    void queryShowsFirstNameRouteAndRecordings() {
        alert("active", null);
        LocationPoint dot = new LocationPoint(new BigDecimal("4.61"), new BigDecimal("-74.08"), null, NOW);
        when(alerts.route(5L)).thenReturn(List.of(dot));
        when(evidences.ofAlert(5L)).thenReturn(List.of(new EvidenceSummary(9L, "video", "video/mp4", 100L, NOW)));

        TrackingView v = service.query(CODE, NOW);

        assertEquals("Ana", v.firstName());
        assertTrue(v.active());
        assertEquals(dot, v.lastPoint());
        assertEquals("123", v.emergencyNumber());
        assertEquals(9L, v.evidence().get(0).id());
        // An alert she activated does not expose its message
        assertEquals("panic_button", v.activationMethod());
        assertNull(v.reason());
    }

    @Test
    void withoutRouteUsesTheActivationLocation() {
        alert("active", null);
        when(alerts.route(5L)).thenReturn(List.of());
        when(evidences.ofAlert(5L)).thenReturn(List.of());

        assertEquals(new BigDecimal("4.6"), service.query(CODE, NOW).lastPoint().latitude());
    }

    @Test
    void expiresTwoHoursAfterEnding() {
        alert("resolved", NOW.minusHours(3));

        assertThrows(ResourceNotFoundException.class, () -> service.query(CODE, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.messages(CODE, 0L, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.evidence(CODE, 9L, NOW));
    }

    @Test
    void rejectsRevokedMissingOrMalformedLinks() {
        alert("active", null);
        link.setRevokedAt(NOW);

        assertThrows(ResourceNotFoundException.class, () -> service.query(CODE, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.query("corto", NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.query(TrackingLink.generateCode(), NOW));
    }

    @Test
    void evidenceOfAnotherAlertDoesNotExist() {
        alert("active", null);
        when(evidences.file(5L, 99L)).thenReturn(Optional.empty());
        EvidenceFile file = new EvidenceFile("video/mp4", "v.mp4", null, Optional.empty());
        when(evidences.file(5L, 9L)).thenReturn(Optional.of(file));

        assertSame(file, service.evidence(CODE, 9L, NOW));
        assertThrows(ResourceNotFoundException.class, () -> service.evidence(CODE, 99L, NOW));
    }

    @Test
    void theContactWritesInTheChatButCannotFloodIt() {
        alert("active", null);
        AlertMessageView m = new AlertMessageView(1L, "contact", "Bea", "Hola", NOW);
        when(alerts.contactMessage(5L, "Bea", "Hola")).thenReturn(m);

        assertSame(m, service.sendMessage(CODE, "Bea", "Hola", NOW));
        assertThrows(IllegalArgumentException.class, () -> service.sendMessage(CODE, "Bea", "Hola", NOW.plusSeconds(1)));
        verify(alerts, times(1)).contactMessage(any(), any(), any());
    }
}

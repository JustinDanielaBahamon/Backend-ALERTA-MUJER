package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.web.AuditSource;
import com.alertamujer.alerts.api.AlertWarningEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Check-in of the active alerts: unanswered at the configured minutes, closed at the absolute limit. */
class AlertExpirationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    private final AlertRepository repository = mock(AlertRepository.class);
    private final SettingsPort settings = mock(SettingsPort.class);
    private final AlertEventsPort events = mock(AlertEventsPort.class);
    private final AlertExpirationService service = new AlertExpirationService(repository, settings, events, 120);

    private static Alert alert(LocalDateTime start) {
        Alert a = new Alert();
        a.setStartedAt(start);
        return a;
    }

    @Test
    void atTheCheckInTimeItIsNotClosedButTheContactsAreToldSheDoesNotAnswer() {
        Alert unanswered = alert(NOW.minusMinutes(31));
        Alert recent = alert(NOW.minusMinutes(10));
        when(settings.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of(unanswered, recent));

        assertEquals(0, service.review(NOW));

        assertEquals("active", unanswered.getStatus());
        assertEquals(NOW, unanswered.getUnansweredAt());
        assertNull(recent.getUnansweredAt());
        verify(repository).saveAll(List.of(unanswered));
        verify(events).alertWarning(unanswered, AlertWarningEvent.UNANSWERED, NOW);
        verifyNoMoreInteractions(events);
    }

    @Test
    void theContactsAreToldOnlyOnce() {
        Alert unanswered = alert(NOW.minusMinutes(50));
        unanswered.setUnansweredAt(NOW.minusMinutes(20));
        when(settings.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of(unanswered));

        service.review(NOW);

        verifyNoInteractions(events);
    }

    @Test
    void anExtendedAlertIsAskedAgainAtItsNewTime() {
        Alert extended = alert(NOW.minusMinutes(40));
        extended.setExtendedUntil(NOW.plusMinutes(20));
        when(settings.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of(extended));

        service.review(NOW);

        assertNull(extended.getUnansweredAt());
        verifyNoInteractions(events);
    }

    @Test
    void itIsClosedAtTheAbsoluteLimitOfTwoHours() {
        Alert old = alert(NOW.minusMinutes(121));
        old.setUnansweredAt(NOW.minusMinutes(90));
        when(settings.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of(old));

        assertEquals(1, service.review(NOW));

        assertEquals("expired", old.getStatus());
        assertEquals(NOW, old.getEndedAt());
        verify(events).alertEnded(old, NOW);
        assertEquals(NOW, service.lastRun());
        assertEquals(1, service.expiredLastRun());
    }

    @Test
    void withoutConfigurationExpiresNothing() {
        when(settings.maxAlertDurationMinutes()).thenReturn(0);

        assertEquals(0, service.review(NOW));
        verifyNoInteractions(repository, events);
    }

    @Test
    void theRunRecordsTheTaskSourceForTheAudit() {
        when(settings.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of());
        String[] source = new String[1];
        when(repository.saveAll(List.of())).thenAnswer(inv -> {
            source[0] = AuditSource.current();
            return List.of();
        });

        service.review(NOW);

        assertEquals("Tarea automática · Expiración de alertas", source[0]);
        assertNull(AuditSource.current());
    }
}

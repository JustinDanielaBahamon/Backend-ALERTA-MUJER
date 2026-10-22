package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.web.AuditSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertExpirationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Mock private AlertRepository repository;
    @Mock private SettingsPort settings;
    @Mock private AlertEventsPort events;

    @InjectMocks private AlertExpirationService service;

    private static Alert alert(LocalDateTime start) {
        Alert a = new Alert();
        a.setStartedAt(start);
        return a;
    }

    @Test
    void expiresOnlyThoseOverTheMaxDurationAndNotifies() {
        Alert old = alert(NOW.minusMinutes(31));
        Alert recent = alert(NOW.minusMinutes(10));
        when(settings.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of(old, recent));

        assertEquals(1, service.review(NOW));

        assertEquals("expired", old.getStatus());
        assertEquals(NOW, old.getEndedAt());
        assertEquals("active", recent.getStatus());
        verify(repository).saveAll(List.of(old));
        verify(events).alertEnded(old, NOW);
        verifyNoMoreInteractions(events);
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

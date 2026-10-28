package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertWarningEvent;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.SettingsPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** "Sigo en peligro" and the duress PIN. */
class AlertCheckInServiceTest {

    private final AlertRepository repository = mock(AlertRepository.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final SettingsPort settings = mock(SettingsPort.class);
    private final AlertEventsPort events = mock(AlertEventsPort.class);
    private final AlertCheckInService service = new AlertCheckInService(repository, identity, settings, events, 120);

    private Alert alert;

    @BeforeEach
    void setUp() {
        alert = new Alert();
        alert.setId(5L);
        alert.setUserProfileId(10L);
        alert.setStartedAt(LocalDateTime.now().minusMinutes(26));
        when(identity.requiredProfile(1L)).thenReturn(10L);
        when(repository.find(5L)).thenReturn(Optional.of(alert));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(settings.maxAlertDurationMinutes()).thenReturn(30);
    }

    @Test
    void stillInDangerExtendsHerAlert() {
        Alert extended = service.extend(5L, 1L);

        assertNotNull(extended.getExtendedUntil());
        assertTrue(extended.getExtendedUntil().isAfter(LocalDateTime.now().plusMinutes(30)));
        verifyNoInteractions(events);
    }

    @Test
    void duressKeepsTheAlertActiveAndWarnsTheContactsOnce() {
        service.duress(5L, 1L);
        service.duress(5L, 1L);

        assertEquals("active", alert.getStatus());
        assertNotNull(alert.getDuressAt());
        verify(events, times(1)).alertWarning(eq(alert), eq(AlertWarningEvent.DURESS), any());
    }

    @Test
    void anotherUsersAlertIsNotFound() {
        when(identity.requiredProfile(2L)).thenReturn(99L);

        assertThrows(ResourceNotFoundException.class, () -> service.extend(5L, 2L));
        assertThrows(ResourceNotFoundException.class, () -> service.duress(5L, 2L));
    }
}

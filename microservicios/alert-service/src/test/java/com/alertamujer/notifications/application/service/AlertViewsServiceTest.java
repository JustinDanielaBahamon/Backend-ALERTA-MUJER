package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.application.port.out.ViewsRepository;
import com.alertamujer.notifications.application.port.out.ViewsRepository.ViewCount;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertViewsServiceTest {

    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final ViewsRepository views = mock(ViewsRepository.class);
    private final AlertViewsService service = new AlertViewsService(alerts, identity, views);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 22, 10, 0);

    private void alertOfProfile(long profile) {
        when(alerts.find(5L)).thenReturn(Optional.of(
                new AlertSummary(5L, profile, "main", "panic_button", "active", null, null, null, null, null, null)));
    }

    @Test
    void theOwnerSeesHowManyOpenedAndHowManyAreWatching() {
        alertOfProfile(10L);
        when(identity.profileOf(1L)).thenReturn(Optional.of(10L));
        when(views.count(5L, NOW.minusSeconds(90))).thenReturn(new ViewCount(3, 1));

        assertEquals(new ViewCount(3, 1), service.views(5L, 1L, NOW));
    }

    @Test
    void anotherPersonCannotSeeThem() {
        alertOfProfile(10L);
        when(identity.profileOf(2L)).thenReturn(Optional.of(20L));

        assertThrows(ResourceNotFoundException.class, () -> service.views(5L, 2L, NOW));
        verifyNoInteractions(views);
    }
}

package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.in.NotifyContactsUseCase;
import com.alertamujer.notifications.application.port.out.EscalationRepository;
import com.alertamujer.notifications.application.port.out.EscalationRepository.UnseenAlert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Escalation of alerts nobody opened, with mocked ports. */
class AlertEscalationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 23, 22, 0);

    private final EscalationRepository escalations = mock(EscalationRepository.class);
    private final NotifyContactsUseCase notices = mock(NotifyContactsUseCase.class);
    private final AlertEscalationService service = new AlertEscalationService(escalations, notices, 5);

    @Test
    void looksForAlertsStartedBetweenAnHourAndFiveMinutesAgo() {
        when(escalations.unseen(any(), any())).thenReturn(List.of());

        service.escalate(NOW);

        verify(escalations).unseen(NOW.minusMinutes(60), NOW.minusMinutes(5));
    }

    @Test
    void resendsTheNoticeOnceItClaimedTheAlert() {
        UnseenAlert alert = new UnseenAlert(5L, 10L, NOW.minusMinutes(6));
        when(escalations.unseen(any(), any())).thenReturn(List.of(alert));
        when(escalations.claim(5L, NOW)).thenReturn(true);

        assertEquals(1, service.escalate(NOW));

        verify(notices).alertReminder(5L, 10L, NOW.minusMinutes(6));
    }

    @Test
    void doesNotResendWhenAnotherInstanceClaimedIt() {
        when(escalations.unseen(any(), any())).thenReturn(List.of(new UnseenAlert(5L, 10L, NOW.minusMinutes(6))));
        when(escalations.claim(5L, NOW)).thenReturn(false);

        assertEquals(0, service.escalate(NOW));

        verifyNoInteractions(notices);
    }

    @Test
    void aFailureDoesNotStopTheOtherAlerts() {
        when(escalations.unseen(any(), any())).thenReturn(List.of(
                new UnseenAlert(5L, 10L, NOW.minusMinutes(9)), new UnseenAlert(6L, 11L, NOW.minusMinutes(7))));
        when(escalations.claim(any(), any())).thenReturn(true);
        doThrow(new RuntimeException("caído")).when(notices).alertReminder(eq(5L), any(), any());

        assertEquals(1, service.escalate(NOW));

        verify(notices).alertReminder(6L, 11L, NOW.minusMinutes(7));
    }
}

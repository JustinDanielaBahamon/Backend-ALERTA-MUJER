package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertWarningEvent;
import com.alertamujer.notifications.application.port.in.NotifyContactsUseCase;
import com.alertamujer.notifications.application.port.out.AlertSignalRepository;
import com.alertamujer.notifications.application.port.out.AlertSignalRepository.SilentAlert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Lost signal during an active alert. */
class AlertSignalWatchServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 24, 9, 0);

    private final AlertSignalRepository signals = mock(AlertSignalRepository.class);
    private final NotifyContactsUseCase notices = mock(NotifyContactsUseCase.class);
    private final AlertSignalWatchService service = new AlertSignalWatchService(signals, notices, 5);

    @Test
    void looksForFiveMinutesOfSilenceInAlertsOfTheLastThreeHours() {
        when(signals.silent(any(), any())).thenReturn(List.of());

        service.review(NOW);

        verify(signals).silent(NOW.minusMinutes(5), NOW.minusHours(3));
    }

    @Test
    void tellsTheContactsWithTheLastSignalOnceItClaimedTheAlert() {
        LocalDateTime last = NOW.minusMinutes(7);
        when(signals.silent(any(), any())).thenReturn(List.of(new SilentAlert(5L, 10L, last)));
        when(signals.claim(5L, NOW)).thenReturn(true);

        assertEquals(1, service.review(NOW));

        verify(notices).alertWarning(5L, 10L, AlertWarningEvent.SIGNAL_LOST, last);
    }

    @Test
    void doesNotTellTwiceWhenAnotherInstanceClaimedIt() {
        when(signals.silent(any(), any())).thenReturn(List.of(new SilentAlert(5L, 10L, NOW.minusMinutes(7))));
        when(signals.claim(5L, NOW)).thenReturn(false);

        assertEquals(0, service.review(NOW));

        verifyNoInteractions(notices);
    }
}

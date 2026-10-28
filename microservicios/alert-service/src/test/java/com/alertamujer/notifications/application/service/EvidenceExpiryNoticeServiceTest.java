package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.out.DevicesPort;
import com.alertamujer.notifications.application.port.out.EvidenceExpiryRepository;
import com.alertamujer.notifications.application.port.out.EvidenceExpiryRepository.UserWithExpiring;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** One notice per user before her evidence is deleted. */
class EvidenceExpiryNoticeServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 24, 10, 0);

    private final EvidenceExpiryRepository evidence = mock(EvidenceExpiryRepository.class);
    private final DevicesPort devices = mock(DevicesPort.class);
    private final PushPort push = mock(PushPort.class);
    private final EvidenceExpiryNoticeService service = new EvidenceExpiryNoticeService(evidence, devices, push, 30);

    @Test
    void looksAtTheEvidenceRecordedTwentyFiveDaysAgo() {
        when(evidence.recordedBetween(any(), any())).thenReturn(List.of());

        service.notice(NOW);

        verify(evidence).recordedBetween(NOW.minusDays(26), NOW.minusDays(25));
    }

    @Test
    void sendsASingleNoticePerUserWithHowManyAlerts() {
        when(evidence.recordedBetween(any(), any())).thenReturn(List.of(new UserWithExpiring(7L, 3), new UserWithExpiring(8L, 1)));
        when(devices.tokensOf(7L)).thenReturn(List.of("token-ana"));
        when(devices.tokensOf(8L)).thenReturn(List.of());

        assertEquals(1, service.notice(NOW));

        verify(push).send(List.of("token-ana"), Map.of("tipo", "EVIDENCIAS_POR_BORRAR", "dias", "5", "alertas", "3"));
        verifyNoMoreInteractions(push);
    }
}

package com.alertamujer.evidence.application.service;

import com.alertamujer.evidence.application.port.out.AlertsPort;
import com.alertamujer.evidence.application.port.out.EvidenceStorage;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvidenceCleanupServiceTest {

    private final EvidenceRepository repository = mock(EvidenceRepository.class);
    private final EvidenceStorage storage = mock(EvidenceStorage.class);
    private final AlertsPort alerts = mock(AlertsPort.class);
    private final EvidenceCleanupService service = new EvidenceCleanupService(repository, storage, alerts, 60);

    private Evidence evidence(long id, long alertId) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(alertId);
        e.setStorageKey("clave-" + id);
        return e;
    }

    @Test
    void deletesOlderThan60DaysExceptFromKeptAlerts() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 19, 4, 0);
        Evidence normal = evidence(1, 10);
        Evidence conservada = evidence(2, 20);
        when(repository.savedBefore(ahora.minusDays(60))).thenReturn(List.of(normal, conservada));
        when(alerts.keepEvidence(Set.of(10L, 20L))).thenReturn(Set.of(20L));

        assertEquals(1, service.limpiar(ahora));

        verify(repository).deleteAll(List.of(normal));
        verify(storage).delete("clave-1");
        verify(storage, never()).delete("clave-2");
    }

    @Test
    void withoutOldEvidenceDoesNothing() {
        when(repository.savedBefore(any())).thenReturn(List.of());

        assertEquals(0, service.limpiar(LocalDateTime.now()));
        verifyNoInteractions(storage, alerts);
    }

    @Test
    void deletedAccount_deletesAllEvidenceEvenIfKept() {
        Evidence a = evidence(1, 10);
        Evidence b = evidence(2, 20);
        when(alerts.idsOfUser(7L)).thenReturn(List.of(10L, 20L));
        when(repository.ofAlerts(List.of(10L, 20L))).thenReturn(List.of(a, b));

        assertEquals(2, service.removeForUser(7L));

        verify(repository).deleteAll(List.of(a, b));
        verify(storage).delete("clave-1");
        verify(storage).delete("clave-2");
        verify(alerts, never()).keepEvidence(any());
    }

    @Test
    void deletedAccountWithoutProfileDoesNothing() {
        assertEquals(0, service.removeForUser(null));
        verifyNoInteractions(repository, storage, alerts);
    }
}

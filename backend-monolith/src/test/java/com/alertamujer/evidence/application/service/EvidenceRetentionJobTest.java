package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.evidence.infrastructure.storage.EvidenceStorage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvidenceRetentionJobTest {

    private final EvidenceRepository repository = mock(EvidenceRepository.class);
    private final EvidenceStorage storage = mock(EvidenceStorage.class);
    private final AlertsApi alertsApi = mock(AlertsApi.class);
    private final EvidenceRetentionJob job = new EvidenceRetentionJob(repository, storage, alertsApi, 60);

    private Evidence evidencia(long id, long alertId) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(alertId);
        e.setStorageKey("clave-" + id);
        return e;
    }

    @Test
    void borraLasDeMasDe60DiasMenosLasDeAlertasConservadas() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 19, 4, 0);
        Evidence normal = evidencia(1, 10);
        Evidence conservada = evidencia(2, 20);
        when(repository.findByCreatedAtBefore(ahora.minusDays(60))).thenReturn(List.of(normal, conservada));
        when(alertsApi.alertsKeepingEvidence(Set.of(10L, 20L))).thenReturn(Set.of(20L));

        assertEquals(1, job.limpiar(ahora));

        verify(repository).deleteAll(List.of(normal));
        verify(storage).delete("clave-1");
        verify(storage, never()).delete("clave-2");
    }

    @Test
    void sinEvidenciasViejasNoHaceNada() {
        when(repository.findByCreatedAtBefore(any())).thenReturn(List.of());

        assertEquals(0, job.limpiar(LocalDateTime.now()));
        verifyNoInteractions(storage, alertsApi);
    }
}

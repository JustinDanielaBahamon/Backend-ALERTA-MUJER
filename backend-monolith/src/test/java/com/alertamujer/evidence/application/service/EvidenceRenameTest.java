package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.evidence.infrastructure.storage.EvidenceStorage;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvidenceRenameTest {

    private final EvidenceRepository repository = mock(EvidenceRepository.class);
    private final AlertsApi alertsApi = mock(AlertsApi.class);
    private final IdentityApi identityApi = mock(IdentityApi.class);
    private final EvidenceService service =
            new EvidenceService(repository, alertsApi, identityApi, mock(EvidenceStorage.class), 1000);

    private Evidence evidencia;

    @BeforeEach
    void preparar() {
        evidencia = new Evidence();
        evidencia.setId(4L);
        evidencia.setAlertId(9L);
        evidencia.setMediaType("video");
        evidencia.setFileName("video_1760870000000.mp4");
        when(repository.findById(4L)).thenReturn(Optional.of(evidencia));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alertsApi.findAlert(9L)).thenReturn(Optional.of(
                new AlertSummary(9L, 70L, "sos", "button", "active", null, null, null, null, null, null)));
        when(identityApi.requireProfileId(1L)).thenReturn(70L);
        when(identityApi.requireProfileId(2L)).thenReturn(71L);
    }

    @Test
    void conservaLaExtensionAlRenombrar() {
        service.renombrar(4L, "  Camino a casa  ", 1L);

        assertEquals("Camino a casa.mp4", evidencia.getFileName());
    }

    @Test
    void rechazaNombresVaciosOMuyLargos() {
        assertThrows(IllegalArgumentException.class, () -> service.renombrar(4L, "   ", 1L));
        assertThrows(IllegalArgumentException.class, () -> service.renombrar(4L, "a".repeat(81), 1L));
    }

    @Test
    void otraUsuariaNoPuedeRenombrar() {
        assertThrows(AccessDeniedException.class, () -> service.renombrar(4L, "x", 2L));
        verify(repository, never()).save(any());
    }
}

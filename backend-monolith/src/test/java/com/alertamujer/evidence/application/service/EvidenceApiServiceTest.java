package com.alertamujer.evidence.application.service;

import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.evidence.infrastructure.storage.EvidenceStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceApiServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private EvidenceStorage storage;

    @InjectMocks
    private EvidenceApiService service;

    private Evidence evidencia(Long id, Long alertId, String key) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(alertId);
        e.setMediaType("audio");
        e.setContentType("audio/mp4");
        e.setStorageKey(key);
        return e;
    }

    @Test
    void soloListaEvidenciasConArchivo() {
        when(evidenceRepository.findByAlertIdOrderByCreatedAtAsc(5L)).thenReturn(List.of(
                evidencia(1L, 5L, "6f1c2a0e-3b7d-4c55-9a64-2b0f3d9e1c11"),
                evidencia(2L, 5L, null)));

        var lista = service.evidenciasDeAlerta(5L);

        assertEquals(1, lista.size());
        assertEquals(1L, lista.get(0).id());
    }

    @Test
    void noEntregaElArchivoDeOtraAlerta() {
        when(evidenceRepository.findById(1L)).thenReturn(Optional.of(evidencia(1L, 7L, "6f1c2a0e-3b7d-4c55-9a64-2b0f3d9e1c11")));

        assertTrue(service.archivo(5L, 1L).isEmpty());
    }
}

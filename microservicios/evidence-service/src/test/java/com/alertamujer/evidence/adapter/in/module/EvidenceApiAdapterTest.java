package com.alertamujer.evidence.adapter.in.module;

import com.alertamujer.evidence.application.port.out.EvidenceStorage;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvidenceApiAdapterTest {

    private final EvidenceRepository repository = mock(EvidenceRepository.class);
    private final EvidenceApiAdapter api = new EvidenceApiAdapter(repository, mock(EvidenceStorage.class));

    private Evidence evidence(Long id, Long alertId, String key) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(alertId);
        e.setMediaType("audio");
        e.setStorageKey(key);
        return e;
    }

    @Test
    void onlyListsEvidenceWithFile() {
        when(repository.ofAlert(5L)).thenReturn(List.of(evidence(1L, 5L, "clave"), evidence(2L, 5L, null)));

        var list = api.alertEvidence(5L);

        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).id());
    }

    @Test
    void doesNotServeTheFileOfAnotherAlert() {
        when(repository.find(1L)).thenReturn(Optional.of(evidence(1L, 7L, "clave")));

        assertTrue(api.file(5L, 1L).isEmpty());
    }
}

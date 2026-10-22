package com.alertamujer.notifications.adapter.out.evidence;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvidenciasAdapterTest {

    private final EvidenceApi api = mock(EvidenceApi.class);
    private final EvidenciasAdapter adapter = new EvidenciasAdapter(api);

    @Test
    void conElServicioDespiertoDevuelveLasGrabaciones() {
        EvidenceSummary video = new EvidenceSummary(1L, "video", "video/mp4", 10L, LocalDateTime.now());
        when(api.alertEvidence(5L)).thenReturn(List.of(video));

        assertEquals(List.of(video), adapter.deAlerta(5L));
    }

    @Test
    void siElServicioTardaNoBloqueaElSeguimiento() {
        when(api.alertEvidence(5L)).thenAnswer(inv -> {
            Thread.sleep(EvidenciasAdapter.ESPERA_MS + 3000);
            return List.of();
        });

        long inicio = System.currentTimeMillis();
        List<EvidenceSummary> list = adapter.deAlerta(5L);

        assertTrue(list.isEmpty());
        assertTrue(System.currentTimeMillis() - inicio < EvidenciasAdapter.ESPERA_MS + 1500, "responde a los ~4 s");
    }
}

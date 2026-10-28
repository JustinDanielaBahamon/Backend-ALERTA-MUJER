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

class EvidenceAdapterTest {

    private final EvidenceApi api = mock(EvidenceApi.class);
    private final EvidenceAdapter adapter = new EvidenceAdapter(api);

    @Test
    void withTheServiceAwakeReturnsTheRecordings() {
        EvidenceSummary video = new EvidenceSummary(1L, "video", "video/mp4", 10L, LocalDateTime.now());
        when(api.alertEvidence(5L)).thenReturn(List.of(video));

        assertEquals(List.of(video), adapter.ofAlert(5L));
    }

    @Test
    void ifTheServiceIsSlowItDoesNotBlockTracking() {
        when(api.alertEvidence(5L)).thenAnswer(inv -> {
            Thread.sleep(EvidenceAdapter.WAIT_MS + 3000);
            return List.of();
        });

        long start = System.currentTimeMillis();
        List<EvidenceSummary> list = adapter.ofAlert(5L);

        assertTrue(list.isEmpty());
        assertTrue(System.currentTimeMillis() - start < EvidenceAdapter.WAIT_MS + 1500, "responde a los ~4 s");
    }
}

package com.alertamujer.notifications.adapter.out.evidence;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;
import com.alertamujer.notifications.application.port.out.EvidencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Output adapter: the recordings of an alert (Evidence Service).
 *
 * The list is awaited at most {@value #WAIT_MS} ms: if Evidence Service is asleep (free Render
 * takes 1-3 min to wake up), the contact's tracking page still shows the map and
 * the data, without recordings. The query keeps running in the background and wakes the service, so the
 * next page refresh already brings them.
 */
@Component
class EvidenceAdapter implements EvidencePort {

    private static final Logger log = LoggerFactory.getLogger(EvidenceAdapter.class);
    static final long WAIT_MS = 4000;

    private final EvidenceApi evidenceApi;
    // Few queries at a time: if they are all busy (service asleep), no more are queued
    private final ExecutorService queries = new ThreadPoolExecutor(1, 4, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());

    EvidenceAdapter(EvidenceApi evidenceApi) {
        this.evidenceApi = evidenceApi;
    }

    @Override
    public List<EvidenceSummary> ofAlert(Long alertId) {
        CompletableFuture<List<EvidenceSummary>> list =
                CompletableFuture.supplyAsync(() -> evidenceApi.alertEvidence(alertId), queries);
        try {
            return list.get(WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception e) {
            log.info("Grabaciones de la alerta {} sin respuesta a tiempo (Evidence Service despertando): {}",
                    alertId, e.getClass().getSimpleName());
            return List.of();
        }
    }

    @Override
    public Optional<EvidenceFile> file(Long alertId, Long evidenceId) {
        return evidenceApi.file(alertId, evidenceId);
    }
}

package com.alertamujer.notifications.adapter.out.evidence;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;
import com.alertamujer.notifications.application.port.out.EvidenciasPort;
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
 * Adaptador de salida: las grabaciones de una alerta (Evidence Service).
 *
 * La lista se espera como máximo {@value #ESPERA_MS} ms: si Evidence Service está dormido (Render
 * gratis tarda 1-3 min en despertar), el seguimiento del contacto se muestra igual con el mapa y
 * los datos, sin grabaciones. La consulta sigue en segundo plano y despierta al servicio, así que la
 * siguiente actualización de la página ya las trae.
 */
@Component
class EvidenciasAdapter implements EvidenciasPort {

    private static final Logger log = LoggerFactory.getLogger(EvidenciasAdapter.class);
    static final long ESPERA_MS = 4000;

    private final EvidenceApi evidenceApi;
    // Pocas consultas a la vez: si están todas ocupadas (servicio dormido), no se encolan más
    private final ExecutorService consultas = new ThreadPoolExecutor(1, 4, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());

    EvidenciasAdapter(EvidenceApi evidenceApi) {
        this.evidenceApi = evidenceApi;
    }

    @Override
    public List<EvidenceSummary> deAlerta(Long alertId) {
        CompletableFuture<List<EvidenceSummary>> list =
                CompletableFuture.supplyAsync(() -> evidenceApi.alertEvidence(alertId), consultas);
        try {
            return list.get(ESPERA_MS, TimeUnit.MILLISECONDS);
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
    public Optional<EvidenceFile> file(Long alertId, Long evidenciaId) {
        return evidenceApi.file(alertId, evidenciaId);
    }
}

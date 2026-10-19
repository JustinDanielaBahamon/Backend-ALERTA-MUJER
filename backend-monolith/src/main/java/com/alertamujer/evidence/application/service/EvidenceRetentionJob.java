package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.evidence.infrastructure.storage.EvidenceStorage;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Borra las evidencias (archivo en la nube y registro) a los 60 días, como promete la app, salvo las
 * de alertas que la usuaria marcó para conservar (p. ej. porque va a poner una denuncia). Así el
 * almacenamiento (Backblaze B2, 10 GB gratis) no se llena.
 */
@Component
public class EvidenceRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(EvidenceRetentionJob.class);

    private final EvidenceRepository repository;
    private final EvidenceStorage storage;
    private final AlertsApi alertsApi;
    private final int dias;

    public EvidenceRetentionJob(EvidenceRepository repository, EvidenceStorage storage, AlertsApi alertsApi,
                                @Value("${evidence.retention.days:60}") int dias) {
        this.repository = repository;
        this.storage = storage;
        this.alertsApi = alertsApi;
        this.dias = dias;
    }

    // Todos los días a las 4:00 a. m. (hora de Colombia)
    @Scheduled(cron = "${evidence.retention.cron:0 0 4 * * *}", zone = "America/Bogota")
    public void limpiar() {
        limpiar(LocalDateTime.now());
    }

    int limpiar(LocalDateTime ahora) {
        List<Evidence> viejas = repository.findByCreatedAtBefore(ahora.minusDays(dias));
        if (viejas.isEmpty()) return 0;
        Set<Long> conservadas = alertsApi.alertsKeepingEvidence(
                viejas.stream().map(Evidence::getAlertId).collect(Collectors.toSet()));
        List<Evidence> aBorrar = viejas.stream().filter(e -> !conservadas.contains(e.getAlertId())).toList();
        if (aBorrar.isEmpty()) return 0;
        AuditSource.con("Tarea automática · Limpieza de evidencias", () -> {
            repository.deleteAll(aBorrar);
            return null;
        });
        // El archivo se borra después del registro: si falla, queda un archivo huérfano pero nunca un
        // registro que apunte a un archivo que ya no existe
        aBorrar.forEach(e -> {
            try {
                storage.delete(e.getStorageKey());
            } catch (RuntimeException ex) {
                log.warn("No se pudo borrar el archivo de la evidencia {}: {}", e.getId(), ex.getMessage());
            }
        });
        log.info("Evidencias de más de {} días eliminadas: {}", dias, aBorrar.size());
        return aBorrar.size();
    }
}

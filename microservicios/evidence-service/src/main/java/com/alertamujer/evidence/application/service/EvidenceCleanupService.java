package com.alertamujer.evidence.application.service;

import com.alertamujer.evidence.application.port.in.CleanUpEvidenceUseCase;
import com.alertamujer.evidence.application.port.out.AlertsPort;
import com.alertamujer.evidence.application.port.out.EvidenceStorage;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deletes evidence (file and record) after 60 days, as the app promises, except for alerts the user
 * marked to keep. This way the storage (B2, 10 GB free) does not fill up.
 */
@Service
public class EvidenceCleanupService implements CleanUpEvidenceUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvidenceCleanupService.class);

    private final EvidenceRepository repository;
    private final EvidenceStorage storage;
    private final AlertsPort alerts;
    private final int days;

    public EvidenceCleanupService(EvidenceRepository repository, EvidenceStorage storage,
                                     AlertsPort alerts, @Value("${evidence.retention.days:60}") int days) {
        this.repository = repository;
        this.storage = storage;
        this.alerts = alerts;
        this.days = days;
    }

    @Override
    public int clear(LocalDateTime ahora) {
        List<Evidence> viejas = repository.savedBefore(ahora.minusDays(days));
        if (viejas.isEmpty()) return 0;
        Set<Long> kept = alerts.keepEvidence(
                viejas.stream().map(Evidence::getAlertId).collect(Collectors.toSet()));
        List<Evidence> toDelete = viejas.stream().filter(e -> !kept.contains(e.getAlertId())).toList();
        if (toDelete.isEmpty()) return 0;
        remove(toDelete, "Tarea automática · Limpieza de evidencias");
        log.info("Evidencias de más de {} días eliminadas: {}", days, toDelete.size());
        return toDelete.size();
    }

    @Override
    public int removeForUser(Long userProfileId) {
        if (userProfileId == null) return 0;
        List<Evidence> suyas = repository.ofAlerts(alerts.idsOfUser(userProfileId));
        if (suyas.isEmpty()) return 0;
        remove(suyas, "Cuenta eliminada · Borrado de evidencias");
        log.info("Cuenta eliminada: {} evidencias del perfil {} borradas", suyas.size(), userProfileId);
        return suyas.size();
    }

    private void remove(List<Evidence> evidences, String source) {
        AuditSource.runAs(source, () -> {
            repository.deleteAll(evidences);
            return null;
        });
        // The file is deleted after the record: if that fails an orphan file is left, never a
        // record pointing to a file that no longer exists
        evidences.forEach(e -> {
            try {
                storage.delete(e.getStorageKey());
            } catch (RuntimeException ex) {
                log.warn("No se pudo borrar el archivo de la evidencia {}: {}", e.getId(), ex.getMessage());
            }
        });
    }
}

package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.QueryEvidenceUseCase;
import com.alertamujer.evidence.application.port.in.EvidenceWithAlert;
import com.alertamujer.evidence.application.port.in.UpdateEvidenceUseCase;
import com.alertamujer.evidence.application.port.in.UploadEvidenceUseCase;
import com.alertamujer.evidence.application.port.out.AlertsPort;
import com.alertamujer.evidence.application.port.out.EvidenceStorage;
import com.alertamujer.evidence.application.port.out.EvidenceStorage.IncomingFile;
import com.alertamujer.evidence.application.port.out.EvidenceStorage.StoredFile;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.application.port.out.IdentityPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** User use cases for their evidence. Only knows ports, not the database or the storage. */
@Service
public class EvidenceService implements UploadEvidenceUseCase, QueryEvidenceUseCase, UpdateEvidenceUseCase {

    private final EvidenceRepository repository;
    private final EvidenceStorage storage;
    private final AlertsPort alerts;
    private final IdentityPort identity;
    private final long maxSizeBytes;

    public EvidenceService(EvidenceRepository repository, EvidenceStorage storage,
                             AlertsPort alerts, IdentityPort identity,
                             @Value("${evidence.max-size-bytes}") long maxSizeBytes) {
        this.repository = repository;
        this.storage = storage;
        this.alerts = alerts;
        this.identity = identity;
        this.maxSizeBytes = maxSizeBytes;
    }

    // ── Subir ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public EvidenceWithAlert upload(IncomingFile file, Long alertId, Long userId) {
        if (alertId == null) throw new IllegalArgumentException("Indica a qué alerta pertenece la evidencia");
        if (file == null || file.size() <= 0) throw new IllegalArgumentException("El archivo está vacío");
        if (file.size() > maxSizeBytes) {
            throw new IllegalArgumentException("El archivo supera el máximo de " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }
        String contentType = file.contentType() == null ? "" : file.contentType().toLowerCase();
        String type = Evidence.typeOf(contentType);
        AlertSummary alert = ownAlert(alertId, userId);

        StoredFile stored = storage.save(file);
        try {
            Evidence e = new Evidence();
            e.setAlertId(alert.id());
            e.setMediaType(type);
            e.setContentType(contentType);
            e.setFileName(Evidence.safeName(file.originalName(), type));
            e.setSizeBytes(stored.size());
            e.setSha256(stored.sha256());
            e.setStorageKey(stored.key());
            e.setFileUrl("pending");
            e = repository.save(e);
            e.setFileUrl("/api/evidences/" + e.getId() + "/file");
            return new EvidenceWithAlert(repository.save(e), alert);
        } catch (RuntimeException ex) {
            storage.delete(stored.key()); // without a record there must be no orphan file left
            throw ex;
        }
    }

    // ── Consultar ───────────────────────────────────────────────────

    @Override
    public List<EvidenceWithAlert> ofUser(Long userId) {
        List<Long> ids = alerts.idsOfUser(identity.profileOf(userId));
        List<Evidence> evidences = repository.ofAlerts(ids);
        Map<Long, AlertSummary> byId = alerts.findMany(
                evidences.stream().map(Evidence::getAlertId).collect(Collectors.toSet()));
        return evidences.stream().map(e -> new EvidenceWithAlert(e, byId.get(e.getAlertId()))).toList();
    }

    @Override
    public Evidence una(Long id, Long userId) {
        Evidence e = existing(id);
        ownAlert(e.getAlertId(), userId);
        return e;
    }

    @Override
    public EvidenceWithContent file(Long id, Long userId, boolean admin) {
        Evidence e = admin ? existing(id) : una(id, userId);
        if (!e.hasFile()) throw new ResourceNotFoundException("Archivo de la evidencia " + id);
        Resource content = storage.load(e.getStorageKey());
        if (!content.exists()) throw new ResourceNotFoundException("Archivo de la evidencia " + id);
        return new EvidenceWithContent(e, content);
    }

    // ── Modificar ───────────────────────────────────────────────────

    @Override
    @Transactional
    public EvidenceWithAlert renombrar(Long id, String name, Long userId) {
        Evidence e = una(id, userId);
        e.renombrar(name);
        return new EvidenceWithAlert(repository.save(e), ownAlert(e.getAlertId(), userId));
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId, boolean admin) {
        Evidence e = admin ? existing(id) : una(id, userId);
        repository.delete(e);
        storage.delete(e.getStorageKey());
    }

    @Override
    public Evidence registerWithoutFile(Long alertId, String mediaType, String fileUrl, Long userId) {
        ownAlert(alertId, userId);
        Evidence e = new Evidence();
        e.setAlertId(alertId);
        e.setMediaType(mediaType);
        e.setFileUrl(fileUrl);
        return repository.save(e);
    }

    @Override
    public Evidence updateWithoutFile(Long id, String mediaType, String fileUrl, Long userId) {
        Evidence e = una(id, userId);
        if (e.hasFile()) throw new IllegalArgumentException("Una evidencia con archivo no se puede modificar");
        e.setMediaType(mediaType);
        e.setFileUrl(fileUrl);
        return repository.save(e);
    }

    // ── Apoyo ───────────────────────────────────────────────────────

    private Evidence existing(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
    }

    private AlertSummary ownAlert(Long alertId, Long userId) {
        AlertSummary alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        if (!alert.userProfileId().equals(identity.profileOf(userId))) {
            throw new AccessDeniedException("No puedes acceder a evidencias de alertas de otro usuario");
        }
        return alert;
    }
}

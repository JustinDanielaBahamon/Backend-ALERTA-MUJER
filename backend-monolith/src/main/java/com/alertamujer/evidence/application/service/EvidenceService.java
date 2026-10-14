package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.controller.dto.EvidenceView;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.evidence.infrastructure.storage.EvidenceStorage;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EvidenceService {

    // SVG/HTML quedan fuera a propósito: pueden ejecutar scripts al abrirse
    private static final Set<String> DOCUMENT_TYPES = Set.of("application/pdf");

    private final EvidenceRepository evidenceRepository;
    private final AlertRepository alertRepository;
    private final UserProfileRepository userProfileRepository;
    private final EvidenceStorage storage;
    private final long maxSizeBytes;

    public EvidenceService(EvidenceRepository evidenceRepository, AlertRepository alertRepository,
                           UserProfileRepository userProfileRepository, EvidenceStorage storage,
                           @Value("${evidence.max-size-bytes}") long maxSizeBytes) {
        this.evidenceRepository = evidenceRepository;
        this.alertRepository = alertRepository;
        this.userProfileRepository = userProfileRepository;
        this.storage = storage;
        this.maxSizeBytes = maxSizeBytes;
    }

    // El JWT trae users.id; las alertas pertenecen a user_profile.id.
    private Long profileIdOf(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private Alert ownedAlert(Long alertId, Long currentUserId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        if (!alert.getUserProfileId().equals(profileIdOf(currentUserId))) {
            throw new AccessDeniedException("No puedes acceder a evidencias de alertas de otro usuario");
        }
        return alert;
    }

    public List<Evidence> getAllEvidences() {
        return evidenceRepository.findAll();
    }

    public Evidence getEvidenceById(Long id, Long currentUserId) {
        Evidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
        ownedAlert(evidence.getAlertId(), currentUserId);
        return evidence;
    }

    /** Evidencias de la usuaria autenticada, con un resumen de la alerta de cada una. */
    public List<EvidenceView> getMine(Long currentUserId) {
        Long profileId = profileIdOf(currentUserId);
        List<Evidence> evidencias = evidenceRepository.findByOwnerProfileId(profileId);
        Map<Long, Alert> alertas = alertRepository.findAllById(
                        evidencias.stream().map(Evidence::getAlertId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Alert::getId, Function.identity()));
        return evidencias.stream().map(e -> EvidenceView.of(e, alertas.get(e.getAlertId()))).toList();
    }

    @Transactional
    public EvidenceView upload(MultipartFile file, Long alertId, Long currentUserId) {
        if (alertId == null) throw new IllegalArgumentException("Indica a qué alerta pertenece la evidencia");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("El archivo está vacío");
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("El archivo supera el máximo de " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String mediaType = mediaTypeOf(contentType);
        Alert alert = ownedAlert(alertId, currentUserId);

        EvidenceStorage.StoredFile stored = storage.store(file);
        try {
            Evidence evidence = new Evidence();
            evidence.setAlertId(alert.getId());
            evidence.setMediaType(mediaType);
            evidence.setContentType(contentType);
            evidence.setFileName(nombreSeguro(file.getOriginalFilename(), mediaType));
            evidence.setSizeBytes(stored.size());
            evidence.setSha256(stored.sha256());
            evidence.setStorageKey(stored.key());
            evidence.setFileUrl("pending");
            evidence = evidenceRepository.save(evidence);
            evidence.setFileUrl("/api/evidences/" + evidence.getId() + "/file");
            return EvidenceView.of(evidenceRepository.save(evidence), alert);
        } catch (RuntimeException e) {
            storage.delete(stored.key()); // sin registro no debe quedar un archivo huérfano
            throw e;
        }
    }

    public StoredEvidence getFile(Long id, Long currentUserId) {
        Evidence evidence = getEvidenceById(id, currentUserId);
        if (evidence.getStorageKey() == null) throw new ResourceNotFoundException("Archivo de la evidencia " + id);
        Resource resource = storage.load(evidence.getStorageKey());
        if (!resource.exists()) throw new ResourceNotFoundException("Archivo de la evidencia " + id);
        return new StoredEvidence(evidence, resource);
    }

    public record StoredEvidence(Evidence evidence, Resource resource) {}

    /** Registro sin archivo (compatibilidad con clientes que solo envían una URL). */
    public Evidence createEvidence(Evidence evidence, Long currentUserId) {
        ownedAlert(evidence.getAlertId(), currentUserId);
        evidence.setId(null);
        evidence.setStorageKey(null);
        return evidenceRepository.save(evidence);
    }

    public Evidence updateEvidence(Long id, Evidence changes, Long currentUserId) {
        Evidence existing = getEvidenceById(id, currentUserId);
        if (existing.getStorageKey() != null) {
            throw new IllegalArgumentException("Una evidencia con archivo no se puede modificar");
        }
        existing.setMediaType(changes.getMediaType());
        existing.setFileUrl(changes.getFileUrl());
        return evidenceRepository.save(existing);
    }

    @Transactional
    public void deleteEvidence(Long id, Long currentUserId) {
        Evidence evidence = getEvidenceById(id, currentUserId);
        evidenceRepository.delete(evidence);
        storage.delete(evidence.getStorageKey());
    }

    private static String mediaTypeOf(String contentType) {
        if (contentType.startsWith("image/") && !contentType.contains("svg")) return "photo";
        if (contentType.startsWith("video/")) return "video";
        if (contentType.startsWith("audio/")) return "audio";
        if (DOCUMENT_TYPES.contains(contentType)) return "document";
        throw new IllegalArgumentException("Tipo de archivo no permitido. Sube una foto, video, audio o PDF.");
    }

    // Solo se conserva un nombre legible; nunca se usa para la ruta en disco
    private static String nombreSeguro(String original, String mediaType) {
        String nombre = original == null ? "" : original.replaceAll("[\\\\/\\r\\n\"]", "_").trim();
        if (nombre.isEmpty()) nombre = mediaType;
        return nombre.length() > 200 ? nombre.substring(nombre.length() - 200) : nombre;
    }
}

package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Evidencias para el panel de administración: el archivo, la usuaria dueña y la alerta a la que
 * pertenece, más el estado de revisión que marca la administración.
 */
@Service
public class AdminEvidenceService {

    private static final Set<String> REVISIONES = Set.of("pending", "verified", "rejected");

    private final EvidenceRepository evidenceRepository;
    private final AlertsApi alertsApi;
    private final IdentityApi identityApi;

    public AdminEvidenceService(EvidenceRepository evidenceRepository, AlertsApi alertsApi,
                                IdentityApi identityApi) {
        this.evidenceRepository = evidenceRepository;
        this.alertsApi = alertsApi;
        this.identityApi = identityApi;
    }

    @Transactional(readOnly = true)
    public List<AdminEvidenceView> listar() {
        Map<Long, AlertSummary> alertas = alertsApi.alertsById();
        Map<Long, UserSummary> usuariaPorPerfil = identityApi.usersByProfileId();

        return evidenceRepository.findAll().stream()
                .sorted(Comparator.comparing(Evidence::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(e -> {
                    AlertSummary a = alertas.get(e.getAlertId());
                    UserSummary u = a == null ? null : usuariaPorPerfil.get(a.userProfileId());
                    return vista(e, a, u);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public EvidenceStats estadisticas() {
        List<Evidence> todas = evidenceRepository.findAll();
        long verificadas = todas.stream().filter(e -> "verified".equals(e.getReviewStatus())).count();
        long rechazadas = todas.stream().filter(e -> "rejected".equals(e.getReviewStatus())).count();
        long tamanio = todas.stream().mapToLong(e -> e.getSizeBytes() == null ? 0 : e.getSizeBytes()).sum();
        return new EvidenceStats(todas.size(), verificadas, todas.size() - verificadas - rechazadas, rechazadas, tamanio);
    }

    @Transactional
    public AdminEvidenceView cambiarRevision(Long id, String revision) {
        if (revision == null || !REVISIONES.contains(revision)) {
            throw new IllegalArgumentException("Estado de revisión no válido: " + revision);
        }
        Evidence e = evidenceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
        e.setReviewStatus(revision);
        evidenceRepository.save(e);
        AlertSummary a = alertsApi.findAlert(e.getAlertId()).orElse(null);
        UserSummary u = a == null ? null : identityApi.findUserByProfileId(a.userProfileId()).orElse(null);
        return vista(e, a, u);
    }

    private AdminEvidenceView vista(Evidence e, AlertSummary a, UserSummary u) {
        String nombre = u == null ? null : u.fullName();
        return new AdminEvidenceView(e.getId(), e.getAlertId(), e.getMediaType(), e.getFileName(),
                e.getContentType(), e.getSizeBytes(), e.getSha256(), e.getStorageKey() != null,
                e.getReviewStatus(), e.getCreatedAt(),
                u != null ? u.id() : null, nombre, u != null ? u.email() : null,
                a != null ? a.status() : null, a != null ? a.activationMethod() : null,
                a != null ? a.message() : null, a != null ? a.startedAt() : null,
                a != null ? a.latitude() : null, a != null ? a.longitude() : null);
    }

    /** hasFile indica si hay archivo descargable en /api/evidences/{id}/file. */
    public record AdminEvidenceView(Long id, Long alertId, String mediaType, String fileName, String contentType,
                                    Long sizeBytes, String sha256, boolean hasFile, String reviewStatus,
                                    LocalDateTime createdAt, Long userId, String userName, String userEmail,
                                    String alertStatus, String alertActivationMethod, String alertMessage,
                                    LocalDateTime alertStartedAt, BigDecimal alertLatitude,
                                    BigDecimal alertLongitude) {}

    public record EvidenceStats(long total, long verified, long pending, long rejected, long totalSizeBytes) {}
}

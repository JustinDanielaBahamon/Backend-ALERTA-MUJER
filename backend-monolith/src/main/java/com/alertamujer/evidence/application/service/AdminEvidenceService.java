package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Evidencias para el panel de administración: el archivo, la usuaria dueña y la alerta a la que
 * pertenece, más el estado de revisión que marca la administración.
 */
@Service
public class AdminEvidenceService {

    private static final Set<String> REVISIONES = Set.of("pending", "verified", "rejected");

    private final EvidenceRepository evidenceRepository;
    private final AlertRepository alertRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public AdminEvidenceService(EvidenceRepository evidenceRepository, AlertRepository alertRepository,
                                UserProfileRepository userProfileRepository, UserRepository userRepository) {
        this.evidenceRepository = evidenceRepository;
        this.alertRepository = alertRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminEvidenceView> listar() {
        Map<Long, Alert> alertas = alertRepository.findAll().stream()
                .collect(Collectors.toMap(Alert::getId, Function.identity()));
        Map<Long, Long> usuarioPorPerfil = userProfileRepository.findAll().stream()
                .collect(Collectors.toMap(UserProfile::getId, UserProfile::getUserId, (a, b) -> a));
        Map<Long, User> usuarios = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return evidenceRepository.findAll().stream()
                .sorted(Comparator.comparing(Evidence::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(e -> {
                    Alert a = alertas.get(e.getAlertId());
                    User u = a == null ? null : usuarios.get(usuarioPorPerfil.get(a.getUserProfileId()));
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
        Alert a = alertRepository.findById(e.getAlertId()).orElse(null);
        User u = a == null ? null : userProfileRepository.findById(a.getUserProfileId())
                .flatMap(p -> userRepository.findById(p.getUserId())).orElse(null);
        return vista(e, a, u);
    }

    private AdminEvidenceView vista(Evidence e, Alert a, User u) {
        String nombre = u == null ? null
                : ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                   + (u.getLastName() == null ? "" : u.getLastName())).trim();
        return new AdminEvidenceView(e.getId(), e.getAlertId(), e.getMediaType(), e.getFileName(),
                e.getContentType(), e.getSizeBytes(), e.getSha256(), e.getStorageKey() != null,
                e.getReviewStatus(), e.getCreatedAt(),
                u != null ? u.getId() : null, nombre, u != null ? u.getEmail() : null,
                a != null ? a.getStatus() : null, a != null ? a.getActivationMethod() : null,
                a != null ? a.getMessage() : null, a != null ? a.getStartedAt() : null,
                a != null ? a.getLatitude() : null, a != null ? a.getLongitude() : null);
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

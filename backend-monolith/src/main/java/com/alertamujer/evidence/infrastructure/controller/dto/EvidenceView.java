package com.alertamujer.evidence.infrastructure.controller.dto;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.evidence.domain.model.Evidence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Evidencia con los datos de su alerta, para listarla sin pedir cada alerta por separado. */
public record EvidenceView(
        Long id,
        Long alertId,
        String mediaType,
        String fileName,
        String contentType,
        Long sizeBytes,
        String sha256,
        boolean hasFile,
        LocalDateTime createdAt,
        AlertSummary alert
) {
    public record AlertSummary(Long id, String alertType, String activationMethod, String status,
                               LocalDateTime startedAt, BigDecimal latitude, BigDecimal longitude) {
        static AlertSummary of(Alert a) {
            return a == null ? null : new AlertSummary(a.getId(), a.getAlertType(), a.getActivationMethod(),
                    a.getStatus(), a.getStartedAt(), a.getLatitude(), a.getLongitude());
        }
    }

    public static EvidenceView of(Evidence e, Alert alert) {
        return new EvidenceView(e.getId(), e.getAlertId(), e.getMediaType(), e.getFileName(), e.getContentType(),
                e.getSizeBytes(), e.getSha256(), e.getStorageKey() != null, e.getCreatedAt(), AlertSummary.of(alert));
    }
}

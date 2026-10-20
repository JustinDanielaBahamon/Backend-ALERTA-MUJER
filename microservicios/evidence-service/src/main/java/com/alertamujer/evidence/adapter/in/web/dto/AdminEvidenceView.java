package com.alertamujer.evidence.adapter.in.web.dto;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.AdministrarEvidenciasUseCase.EvidenciaAdmin;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.identity.api.UserSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Evidencia para el panel de administración. hasFile indica si hay archivo en /api/evidences/{id}/file. */
public record AdminEvidenceView(Long id, Long alertId, String mediaType, String fileName, String contentType,
                                Long sizeBytes, String sha256, boolean hasFile, String reviewStatus,
                                LocalDateTime createdAt, Long userId, String userName, String userEmail,
                                String alertStatus, String alertActivationMethod, String alertMessage,
                                LocalDateTime alertStartedAt, BigDecimal alertLatitude, BigDecimal alertLongitude) {

    public static AdminEvidenceView of(EvidenciaAdmin ea) {
        Evidence e = ea.evidencia();
        AlertSummary a = ea.alerta();
        UserSummary u = ea.duena();
        return new AdminEvidenceView(e.getId(), e.getAlertId(), e.getMediaType(), e.getFileName(),
                e.getContentType(), e.getSizeBytes(), e.getSha256(), e.tieneArchivo(), e.getReviewStatus(),
                e.getCreatedAt(), u != null ? u.id() : null, u != null ? u.fullName() : null,
                u != null ? u.email() : null, a != null ? a.status() : null, a != null ? a.activationMethod() : null,
                a != null ? a.message() : null, a != null ? a.startedAt() : null,
                a != null ? a.latitude() : null, a != null ? a.longitude() : null);
    }
}

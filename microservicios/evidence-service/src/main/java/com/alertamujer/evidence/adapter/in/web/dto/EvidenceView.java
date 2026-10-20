package com.alertamujer.evidence.adapter.in.web.dto;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.EvidenciaConAlerta;
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
        AlertInfo alert
) {
    /** Resumen de la alerta que se envía al cliente. */
    public record AlertInfo(Long id, String alertType, String activationMethod, String status,
                            LocalDateTime startedAt, BigDecimal latitude, BigDecimal longitude) {
        static AlertInfo of(AlertSummary a) {
            return a == null ? null : new AlertInfo(a.id(), a.alertType(), a.activationMethod(),
                    a.status(), a.startedAt(), a.latitude(), a.longitude());
        }
    }

    public static EvidenceView of(EvidenciaConAlerta ea) {
        Evidence e = ea.evidencia();
        return new EvidenceView(e.getId(), e.getAlertId(), e.getMediaType(), e.getFileName(), e.getContentType(),
                e.getSizeBytes(), e.getSha256(), e.tieneArchivo(), e.getCreatedAt(), AlertInfo.of(ea.alerta()));
    }
}

package com.alertamujer.evidence.adapter.in.web.dto;

import com.alertamujer.evidence.domain.model.Evidence;

import java.time.LocalDateTime;

/** Evidencia tal como la ve la usuaria (sin la clave interna del almacenamiento). */
public record EvidenceDto(Long id, Long alertId, String mediaType, String fileUrl, String fileName,
                          String contentType, Long sizeBytes, String sha256, String reviewStatus,
                          LocalDateTime createdAt) {

    public static EvidenceDto of(Evidence e) {
        return new EvidenceDto(e.getId(), e.getAlertId(), e.getMediaType(), e.getFileUrl(), e.getFileName(),
                e.getContentType(), e.getSizeBytes(), e.getSha256(), e.getReviewStatus(), e.getCreatedAt());
    }

    /** Cuerpo para registrar o actualizar una evidencia sin archivo (clientes antiguos). */
    public record SinArchivo(Long alertId, String mediaType, String fileUrl) {}
}

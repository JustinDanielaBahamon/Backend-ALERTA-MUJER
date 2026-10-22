package com.alertamujer.evidence.adapter.in.web.dto;

import com.alertamujer.evidence.domain.model.Evidence;

import java.time.LocalDateTime;

/** Evidence as the user sees it (without the internal storage key). */
public record EvidenceDto(Long id, Long alertId, String mediaType, String fileUrl, String fileName,
                          String contentType, Long sizeBytes, String sha256, String reviewStatus,
                          LocalDateTime createdAt) {

    public static EvidenceDto of(Evidence e) {
        return new EvidenceDto(e.getId(), e.getAlertId(), e.getMediaType(), e.getFileUrl(), e.getFileName(),
                e.getContentType(), e.getSizeBytes(), e.getSha256(), e.getReviewStatus(), e.getCreatedAt());
    }

    /** Body to register or update an evidence without a file (old clients). */
    public record WithoutFile(Long alertId, String mediaType, String fileUrl) {}
}

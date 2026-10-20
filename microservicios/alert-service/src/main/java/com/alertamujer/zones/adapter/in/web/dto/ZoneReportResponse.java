package com.alertamujer.zones.adapter.in.web.dto;

import com.alertamujer.zones.domain.model.ZoneReport;

import java.time.LocalDateTime;

public record ZoneReportResponse(Long id, Long zoneId, Long userProfileId, String classification, String comment,
                                 String status, Integer reviewedByAdminId, LocalDateTime reviewedAt, LocalDateTime createdAt) {

    public static ZoneReportResponse of(ZoneReport r) {
        return new ZoneReportResponse(r.getId(), r.getZoneId(), r.getUserProfileId(), r.getClassification(), r.getComment(),
                r.getStatus(), r.getReviewedByAdminId(), r.getReviewedAt(), r.getCreatedAt());
    }
}

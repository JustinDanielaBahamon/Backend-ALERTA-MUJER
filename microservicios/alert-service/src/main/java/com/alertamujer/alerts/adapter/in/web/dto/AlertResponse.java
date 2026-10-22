package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.Alert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Alert as the web and the app receive it (same fields and order as always). */
public record AlertResponse(Long id, Long userProfileId, Long deviceId, String alertType, String activationMethod,
                            String status, String message, BigDecimal latitude, BigDecimal longitude,
                            LocalDateTime startedAt, LocalDateTime endedAt, LocalDateTime cancelledAt,
                            LocalDateTime createdAt, boolean keepEvidence) {

    public static AlertResponse of(Alert a) {
        return new AlertResponse(a.getId(), a.getUserProfileId(), a.getDeviceId(), a.getAlertType(), a.getActivationMethod(),
                a.getStatus(), a.getMessage(), a.getLatitude(), a.getLongitude(), a.getStartedAt(), a.getEndedAt(),
                a.getCancelledAt(), a.getCreatedAt(), a.isKeepEvidence());
    }
}

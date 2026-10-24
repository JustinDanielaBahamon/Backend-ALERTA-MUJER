package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.Alert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Alert as the web and the app receive it. extendedUntil / unansweredAt: the check-in ("¿Sigues en
 * peligro?"); duressAt: she cancelled with her duress PIN (only her own app reads it).
 */
public record AlertResponse(Long id, Long userProfileId, Long deviceId, String alertType, String activationMethod,
                            String status, String message, BigDecimal latitude, BigDecimal longitude,
                            LocalDateTime startedAt, LocalDateTime endedAt, LocalDateTime cancelledAt,
                            LocalDateTime createdAt, boolean keepEvidence, LocalDateTime extendedUntil,
                            LocalDateTime unansweredAt, LocalDateTime duressAt) {

    public static AlertResponse of(Alert a) {
        return new AlertResponse(a.getId(), a.getUserProfileId(), a.getDeviceId(), a.getAlertType(), a.getActivationMethod(),
                a.getStatus(), a.getMessage(), a.getLatitude(), a.getLongitude(), a.getStartedAt(), a.getEndedAt(),
                a.getCancelledAt(), a.getCreatedAt(), a.isKeepEvidence(), a.getExtendedUntil(), a.getUnansweredAt(),
                a.getDuressAt());
    }
}

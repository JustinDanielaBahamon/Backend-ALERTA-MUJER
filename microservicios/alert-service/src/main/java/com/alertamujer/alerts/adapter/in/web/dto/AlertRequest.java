package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.domain.model.AlertChanges;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Body to create (POST) or change (PUT) an alert. The owner comes from the token: an id or
 * userProfileId in the body is ignored. contactIds is only used when creating.
 */
public record AlertRequest(Long deviceId, String alertType, String activationMethod, String status, String message,
                           BigDecimal latitude, BigDecimal longitude, LocalDateTime startedAt,
                           LocalDateTime endedAt, LocalDateTime cancelledAt, List<Long> contactIds) {

    public NewAlert toNew() {
        return new NewAlert(deviceId, alertType, activationMethod, status, message, latitude, longitude, startedAt, contactIds);
    }

    public AlertChanges toChanges() {
        return new AlertChanges(alertType, activationMethod, status, message, endedAt, cancelledAt);
    }
}

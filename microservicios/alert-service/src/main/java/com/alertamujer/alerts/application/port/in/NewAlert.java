package com.alertamujer.alerts.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data to activate an alert. contactIds: who gets notified (individual alert from the
 * contacts module); empty or null = all contacts. The owner comes from the token, never from the body.
 */
public record NewAlert(Long deviceId, String alertType, String activationMethod, String status, String message,
                          BigDecimal latitude, BigDecimal longitude, LocalDateTime startedAt, List<Long> contactIds) {
}

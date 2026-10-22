package com.alertamujer.alerts.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Alert with its user's data, for the admin panel. */
public record AdminAlertView(Long id, Long userProfileId, Long userId, String userName, String userEmail,
                             Long deviceId, String alertType, String activationMethod, String status,
                             String message, BigDecimal latitude, BigDecimal longitude,
                             LocalDateTime startedAt, LocalDateTime endedAt, LocalDateTime cancelledAt,
                             LocalDateTime createdAt) {}

package com.alertamujer.alerts.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GPS point sent by the app. deviceId is optional (older app versions do not send it);
 * recordedAt allows uploading points that were queued while offline.
 */
public record NewLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy,
                             Long alertId, Long deviceId, LocalDateTime recordedAt) {
}

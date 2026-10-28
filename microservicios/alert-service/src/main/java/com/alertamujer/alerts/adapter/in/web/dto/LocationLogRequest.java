package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.application.port.in.NewLocation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GPS point sent by the mobile app. The owner comes from the token, never from the body.
 * deviceId is optional (older app versions do not send it); so is recordedAt: it allows
 * uploading points that were queued while offline.
 */
public record LocationLogRequest(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, Long alertId,
                                 Long deviceId, LocalDateTime recordedAt) {

    public NewLocation toNew() {
        return new NewLocation(latitude, longitude, accuracy, alertId, deviceId, recordedAt);
    }
}

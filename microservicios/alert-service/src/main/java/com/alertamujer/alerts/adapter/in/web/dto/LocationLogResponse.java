package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.domain.model.LocationLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LocationLogResponse(Long id, Long userProfileId, Long alertId, BigDecimal latitude, BigDecimal longitude,
                                  BigDecimal accuracy, LocalDateTime recordedAt) {

    public static LocationLogResponse of(LocationLog l) {
        return new LocationLogResponse(l.getId(), l.getUserProfileId(), l.getAlertId(), l.getLatitude(), l.getLongitude(),
                l.getAccuracy(), l.getRecordedAt());
    }
}

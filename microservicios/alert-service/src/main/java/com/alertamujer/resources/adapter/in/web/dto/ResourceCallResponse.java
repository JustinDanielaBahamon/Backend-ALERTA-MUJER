package com.alertamujer.resources.adapter.in.web.dto;

import com.alertamujer.resources.domain.model.ResourceCall;

import java.time.LocalDateTime;

public record ResourceCallResponse(Long id, Long emergencyResourceId, Long userProfileId, Long alertId, Long deviceId,
                                   String telephoneDialed, String status, LocalDateTime startedAt, LocalDateTime endedAt,
                                   Integer durationSeconds, LocalDateTime createdAt) {

    public static ResourceCallResponse of(ResourceCall c) {
        return new ResourceCallResponse(c.getId(), c.getEmergencyResourceId(), c.getUserProfileId(), c.getAlertId(),
                c.getDeviceId(), c.getTelephoneDialed(), c.getStatus(), c.getStartedAt(), c.getEndedAt(),
                c.getDurationSeconds(), c.getCreatedAt());
    }
}

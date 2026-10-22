package com.alertamujer.resources.adapter.in.web.dto;

import java.time.LocalDateTime;

/** Body to record or update a call. The owner comes from the token: a userProfileId in the body is ignored. */
public record ResourceCallRequest(Long emergencyResourceId, Long alertId, Long deviceId, String telephoneDialed,
                                  String status, LocalDateTime endedAt, Integer durationSeconds) {
}

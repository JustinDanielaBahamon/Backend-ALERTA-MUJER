package com.alertamujer.resources.adapter.in.web.dto;

import java.time.LocalDateTime;

/** Body para registrar o actualizar una llamada. La dueña sale del token: un userProfileId en el body se ignora. */
public record ResourceCallRequest(Long emergencyResourceId, Long alertId, Long deviceId, String telephoneDialed,
                                  String status, LocalDateTime endedAt, Integer durationSeconds) {
}

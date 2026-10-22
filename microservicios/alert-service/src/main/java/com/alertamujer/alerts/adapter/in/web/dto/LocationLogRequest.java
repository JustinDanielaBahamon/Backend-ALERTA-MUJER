package com.alertamujer.alerts.adapter.in.web.dto;

import com.alertamujer.alerts.application.port.in.NewLocation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Punto GPS enviado por la app móvil. La dueña sale del token, nunca del body.
 * deviceId es opcional (versiones anteriores de la app no lo envían); recordedAt también: permite
 * subir puntos que quedaron en cola sin conexión.
 */
public record LocationLogRequest(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, Long alertId,
                                 Long deviceId, LocalDateTime recordedAt) {

    public NewLocation toNew() {
        return new NewLocation(latitude, longitude, accuracy, alertId, deviceId, recordedAt);
    }
}

package com.alertamujer.alerts.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Punto GPS enviado por la app. deviceId es opcional (versiones anteriores de la app no lo envían);
 * recordedAt permite subir puntos que quedaron en cola sin conexión.
 */
public record NuevaUbicacion(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy,
                             Long alertId, Long deviceId, LocalDateTime recordedAt) {
}

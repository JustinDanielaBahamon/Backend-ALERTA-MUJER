package com.alertamujer.alerts.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos para activar una alerta. contactIds: a quién se avisa (alerta individual desde el módulo
 * de contactos); vacío o null = todos los contactos. La dueña sale del token, nunca del body.
 */
public record NewAlert(Long deviceId, String alertType, String activationMethod, String status, String message,
                          BigDecimal latitude, BigDecimal longitude, LocalDateTime startedAt, List<Long> contactIds) {
}

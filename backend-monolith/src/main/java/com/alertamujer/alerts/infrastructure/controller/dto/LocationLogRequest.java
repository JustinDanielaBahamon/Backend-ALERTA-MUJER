package com.alertamujer.alerts.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Punto GPS enviado por la app móvil. La dueña sale del token, nunca del body.
 * recordedAt es opcional: permite subir puntos que quedaron en cola sin conexión.
 */
@Data
public class LocationLogRequest {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracy;
    private Long alertId;
    /** Celular que envía la ubicación (opcional; las versiones anteriores de la app no lo envían). */
    private Long deviceId;
    private LocalDateTime recordedAt;
}

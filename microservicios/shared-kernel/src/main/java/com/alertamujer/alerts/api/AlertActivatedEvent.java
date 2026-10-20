package com.alertamujer.alerts.api;

import java.time.LocalDateTime;

/**
 * Se publica cuando una usuaria activa una alerta y ya quedaron registrados los destinatarios
 * (un registro por contacto y canal). El módulo de notificaciones lo escucha para avisarles.
 */
public record AlertActivatedEvent(Long alertId, Long userProfileId, LocalDateTime startedAt) {}

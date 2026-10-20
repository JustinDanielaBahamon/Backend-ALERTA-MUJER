package com.alertamujer.admin.application.port.in;

import java.time.LocalDateTime;

/** Estado real de las piezas del backend que el panel muestra en Configuración. */
public interface EstadoSistemaUseCase {

    SystemStatus estado();

    /** pushNotifications indica si Firebase está configurado (sin él no salen notificaciones push). */
    record SystemStatus(boolean database, long databaseLatencyMs, boolean pushNotifications,
                        long activeAlerts, LocalDateTime lastExpirationCheck, int lastExpiredCount,
                        LocalDateTime startedAt, int javaVersion) {}
}

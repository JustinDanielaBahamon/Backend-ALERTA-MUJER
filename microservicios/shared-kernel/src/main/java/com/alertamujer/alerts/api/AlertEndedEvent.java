package com.alertamujer.alerts.api;

import java.time.LocalDateTime;

/**
 * Se publica cuando una alerta activa termina: la usuaria o la administración la marcan como
 * atendida (resolved) o cancelada (cancelled), o expira por superar la duración máxima (expired).
 * El módulo de notificaciones lo escucha para avisar a los contactos que ya fueron alertados.
 */
public record AlertEndedEvent(Long alertId, Long userProfileId, String status, LocalDateTime endedAt) {

    /** Estados en los que una alerta activa se considera terminada. */
    public static boolean esFinal(String status) {
        return "resolved".equals(status) || "cancelled".equals(status) || "expired".equals(status);
    }
}

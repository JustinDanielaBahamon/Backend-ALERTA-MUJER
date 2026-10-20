package com.alertamujer.identity.api;

/**
 * Evento que identity publica en cada inicio, intento fallido, cambio de contraseña o cierre de
 * sesión de una cuenta admin. Otro módulo (la auditoría) lo escucha; identity no sabe quién lo usa.
 * En microservicios este evento viajaría por una cola de mensajes.
 */
public record AdminSessionEvent(Long accountId, String action, String detail) {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
}

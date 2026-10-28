package com.alertamujer.identity.api;

/**
 * Event identity publishes on every sign-in, failed attempt, password change or sign-out of an
 * admin account. Another module (the audit) listens to it; identity does not know who uses it.
 * In microservices this event would travel through a message queue.
 */
public record AdminSessionEvent(Long accountId, String action, String detail) {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
}

package com.alertamujer.identity.api;

/**
 * Published when someone asks to recover the password of an existing account. The notifications
 * module listens to it and sends the code by email. The code only travels in memory and in the
 * email: the database keeps its hash.
 */
public record PasswordRecoveryRequestedEvent(String email, String name, String code, int validityMinutes) {}

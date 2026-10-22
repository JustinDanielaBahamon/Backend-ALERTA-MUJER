package com.alertamujer.identity.api;

/**
 * Published when someone asks to recover the password of an existing account. The notifications
 * module listens to it and sends the code by email. The code only travels in memory and in the
 * email: the database keeps its hash.
 */
public record PasswordRecoveryRequestedEvent(String email, String name, @com.fasterxml.jackson.annotation.JsonProperty("codigo") String code, @com.fasterxml.jackson.annotation.JsonProperty("minutosValidez") int validityMinutes) {}

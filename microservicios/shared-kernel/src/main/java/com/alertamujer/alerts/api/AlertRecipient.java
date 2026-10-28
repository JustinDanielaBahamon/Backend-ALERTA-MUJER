package com.alertamujer.alerts.api;

/** A pending delivery of an alert: to which contact, through which channel (email, push) and to which destination. */
public record AlertRecipient(Long id, Long emergencyContactId, String contactName, String channel, String destination) {}

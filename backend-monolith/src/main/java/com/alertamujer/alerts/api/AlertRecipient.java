package com.alertamujer.alerts.api;

/** Un envío pendiente de una alerta: a qué contacto, por qué canal (email, push) y a qué destino. */
public record AlertRecipient(Long id, Long emergencyContactId, String contactName, String channel, String destination) {}

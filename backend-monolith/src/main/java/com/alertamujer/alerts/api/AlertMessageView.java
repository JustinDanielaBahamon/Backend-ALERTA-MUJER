package com.alertamujer.alerts.api;

import java.time.LocalDateTime;

/** Mensaje del chat de una alerta. senderType: "owner" (quien la activó) o "contact". */
public record AlertMessageView(Long id, String senderType, String senderName, String body, LocalDateTime createdAt) {}

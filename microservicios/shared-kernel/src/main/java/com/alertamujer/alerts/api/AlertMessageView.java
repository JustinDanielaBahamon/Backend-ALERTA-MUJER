package com.alertamujer.alerts.api;

import java.time.LocalDateTime;

/** Message of an alert chat. senderType: "owner" (who activated it) or "contact". */
public record AlertMessageView(Long id, String senderType, String senderName, String body, LocalDateTime createdAt) {}

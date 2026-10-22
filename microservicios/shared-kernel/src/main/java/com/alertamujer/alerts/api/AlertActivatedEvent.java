package com.alertamujer.alerts.api;

import java.time.LocalDateTime;

/**
 * Published when a user activates an alert and its recipients are already recorded
 * (one record per contact and channel). The notifications module listens to it to notify them.
 */
public record AlertActivatedEvent(Long alertId, Long userProfileId, LocalDateTime startedAt) {}

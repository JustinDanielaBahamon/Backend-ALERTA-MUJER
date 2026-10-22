package com.alertamujer.alerts.api;

import java.time.LocalDateTime;

/**
 * Published when an active alert ends: the user or the administration marks it as attended
 * (resolved) or cancelled (cancelled), or it expires for exceeding the maximum duration (expired).
 * The notifications module listens to it to tell the contacts who were already alerted.
 */
public record AlertEndedEvent(Long alertId, Long userProfileId, String status, LocalDateTime endedAt) {

    /** Statuses in which an active alert is considered ended. */
    public static boolean esFinal(String status) {
        return "resolved".equals(status) || "cancelled".equals(status) || "expired".equals(status);
    }
}

package com.alertamujer.admin.application.port.in;

import java.time.LocalDateTime;

/** Real status of the backend pieces the panel shows in Settings. */
public interface SystemStatusUseCase {

    SystemStatus status();

    /** pushNotifications tells whether Firebase is configured (without it no push notifications go out). */
    record SystemStatus(boolean database, long databaseLatencyMs, boolean pushNotifications,
                        long activeAlerts, LocalDateTime lastExpirationCheck, int lastExpiredCount,
                        LocalDateTime startedAt, int javaVersion) {}
}

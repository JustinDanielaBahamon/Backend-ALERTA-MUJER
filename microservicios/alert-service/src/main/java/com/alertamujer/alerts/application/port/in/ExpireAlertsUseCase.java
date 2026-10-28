package com.alertamujer.alerts.application.port.in;

import java.time.LocalDateTime;

/**
 * Marks as "expired" the alerts still active beyond the maximum duration configured
 * by the administration, so they are not left open forever.
 */
public interface ExpireAlertsUseCase {

    /** Automatic run: expires the overdue ones and remembers the result for the panel. */
    int review(LocalDateTime now);

    /** Last automatic run (null if it has not run yet). */
    LocalDateTime lastRun();

    int expiredLastRun();
}

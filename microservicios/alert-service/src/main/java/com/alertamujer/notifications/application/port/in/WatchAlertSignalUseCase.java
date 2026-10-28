package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;

/**
 * During an active alert the phone sends its location every 15-60 s. If nothing arrives for a few
 * minutes (phone off, no battery, taken away), the contacts are told with her last location.
 */
public interface WatchAlertSignalUseCase {

    /** Returns how many alerts lost their signal in this review. */
    int review(LocalDateTime now);
}

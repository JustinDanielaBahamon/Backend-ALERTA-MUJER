package com.alertamujer.alerts.application.port.in;

import java.time.LocalDateTime;

/**
 * Cleans the location history: day-to-day data is kept briefly; alert routes are kept
 * longer (they may serve as evidence). The user sees these periods in the app and on the web.
 */
public interface CleanUpLocationsUseCase {

    /** Returns how many locations were deleted. */
    int clear(LocalDateTime now);
}

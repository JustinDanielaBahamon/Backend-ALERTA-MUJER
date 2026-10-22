package com.alertamujer.alerts.application.port.in;

import java.time.LocalDateTime;

/** Periodic review: asks "¿ya llegaste?" and alerts for whoever did not confirm in time. */
public interface ReviewTripsUseCase {

    /** Returns how many alerts it activated. */
    int review(LocalDateTime now);
}

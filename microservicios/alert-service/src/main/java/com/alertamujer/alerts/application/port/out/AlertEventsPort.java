package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.Alert;

import java.time.LocalDateTime;

/**
 * Output port: tells the rest of the system what happens with an alert. The notifications
 * module sends the emails and pushes after the transaction commits.
 */
public interface AlertEventsPort {

    /** The alert was activated and its recipients are already recorded. */
    void alertActivated(Alert alert);

    /** An active alert ended (attended, cancelled or expired). */
    void alertEnded(Alert alert, LocalDateTime when);
}

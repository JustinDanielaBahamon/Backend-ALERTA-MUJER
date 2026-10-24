package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;

/**
 * Notifies the emergency contacts: when the alert is activated, an email with the tracking link and a
 * high-priority push to those who have the app; when it ends, a notice so they stop waiting.
 */
public interface NotifyContactsUseCase {

    /** Stores the result of each delivery in the alert (sent / failed). */
    void alertActivated(Long alertId, Long userProfile, LocalDateTime start);

    /**
     * The alert is still active and no contact opened its tracking: the notice is sent again with a new
     * link. Does not change the recorded delivery status.
     */
    void alertReminder(Long alertId, Long userProfile, LocalDateTime start);

    /**
     * The alert goes on and something changed that the contacts must know (AlertWarningEvent kinds):
     * she does not answer, her phone stopped sending its location (at = last signal), or she
     * "cancelled" under duress. Email and the SOS push again, with a new link.
     */
    void alertWarning(Long alertId, Long userProfile, String kind, LocalDateTime at);

    /** Courtesy notice: does not change the delivery status and a failure is only logged. */
    void alertEnded(Long alertId, Long userProfile, String status, LocalDateTime end);
}

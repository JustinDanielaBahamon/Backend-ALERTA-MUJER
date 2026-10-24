package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;

/**
 * Output port: the app emails. The adapter builds the HTML with the brand template and
 * sends it with the configured provider (Brevo, Resend or log only).
 */
public interface EmailsPort {

    /**
     * Notice to the contact that the user activated an alert. mapsUrl may be null; openedPixelUrl (may be
     * null) is an invisible image that tells the email was opened.
     */
    void alertNotice(String to, String contactName, String userName, LocalDateTime start,
                       String trackingUrl, String mapsUrl, String emergencyNumber, String openedPixelUrl);

    /** Second notice: the alert is still active and nobody opened the tracking yet. reason may be null. */
    void alertReminder(String to, String contactName, String userName, LocalDateTime start,
                       String trackingUrl, String reason, String emergencyNumber);

    /** The alert goes on with a warning (unanswered, signal_lost, duress). at: when it happened. */
    void alertWarning(String to, String contactName, String userName, String kind, LocalDateTime at,
                      String trackingUrl, String emergencyNumber);

    /** Notice that the alert ended. status: resolved, cancelled or expired. */
    void alertEnd(String to, String contactName, String userName, String status, LocalDateTime end,
                     String emergencyNumber);

    void recoveryCode(String to, String name, String code, int validityMinutes);

    /** The provider rejected the email or no provider is configured. */
    class EmailNotSentException extends RuntimeException {
        public EmailNotSentException(String reason) {
            super(reason);
        }
    }
}

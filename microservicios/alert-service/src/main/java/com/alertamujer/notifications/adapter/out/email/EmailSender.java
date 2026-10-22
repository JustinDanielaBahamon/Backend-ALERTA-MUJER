package com.alertamujer.notifications.adapter.out.email;

/** Email sending. The implementation (Resend, Brevo or log only) is chosen by configuration. */
public interface EmailSender {

    /** Sends the email; throws EmailException if the provider rejects it. */
    void send(EmailMessage message);

    /** Provider name, to record it in the deliveries. */
    String provider();

    record EmailMessage(String to, String subject, String html, String text) {}

    class EmailException extends RuntimeException {
        public EmailException(String message) {
            super(message);
        }
    }
}

package com.alertamujer.notifications.adapter.out.email;

/** Envío de correos. La implementación (Resend, Brevo o solo registro) se elige por configuración. */
public interface EmailSender {

    /** Envía el correo; lanza EmailException si el proveedor lo rechaza. */
    void send(EmailMessage message);

    /** Nombre del proveedor, para registrarlo en los envíos. */
    String provider();

    record EmailMessage(String to, String subject, String html, String text) {}

    class EmailException extends RuntimeException {
        public EmailException(String message) {
            super(message);
        }
    }
}

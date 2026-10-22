package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;

/**
 * Puerto de salida: los correos de la app. El adaptador arma el HTML con la plantilla de la marca y
 * lo envía con el proveedor configurado (Brevo, Resend o solo registro).
 */
public interface EmailsPort {

    /** Aviso al contacto de que la usuaria activó una alerta. mapsUrl puede ser null. */
    void alertNotice(String to, String contactName, String userName, LocalDateTime start,
                       String trackingUrl, String mapsUrl, String emergencyNumber);

    /** Aviso de que la alerta terminó. estado: resolved, cancelled o expired. */
    void alertEnd(String to, String contactName, String userName, String status, LocalDateTime end,
                     String emergencyNumber);

    void recoveryCode(String to, String name, String code, int validityMinutes);

    /** El proveedor rechazó el correo o no hay proveedor configurado. */
    class EmailNotSentException extends RuntimeException {
        public EmailNotSentException(String reason) {
            super(reason);
        }
    }
}

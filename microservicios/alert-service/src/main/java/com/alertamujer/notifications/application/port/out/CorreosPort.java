package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;

/**
 * Puerto de salida: los correos de la app. El adaptador arma el HTML con la plantilla de la marca y
 * lo envía con el proveedor configurado (Brevo, Resend o solo registro).
 */
public interface CorreosPort {

    /** Aviso al contacto de que la usuaria activó una alerta. mapsUrl puede ser null. */
    void avisoDeAlerta(String para, String nombreContacto, String nombreUsuaria, LocalDateTime start,
                       String seguimientoUrl, String mapsUrl, String emergencyNumber);

    /** Aviso de que la alerta terminó. estado: resolved, cancelled o expired. */
    void finDeAlerta(String para, String nombreContacto, String nombreUsuaria, String status, LocalDateTime fin,
                     String emergencyNumber);

    void recoveryCode(String para, String name, String code, int validityMinutes);

    /** El proveedor rechazó el correo o no hay proveedor configurado. */
    class CorreoNoEnviadoException extends RuntimeException {
        public CorreoNoEnviadoException(String reason) {
            super(reason);
        }
    }
}

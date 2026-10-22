package com.alertamujer.notifications.application.port.out;

/** Puerto de salida hacia settings (Auth Service por HTTP). */
public interface AjustesPort {

    /** Número de emergencia que se ofrece a los contactos (en Colombia, 123). */
    String emergencyNumber();
}

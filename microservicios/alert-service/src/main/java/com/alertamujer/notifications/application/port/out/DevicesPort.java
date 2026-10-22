package com.alertamujer.notifications.application.port.out;

import java.util.List;

/** Puerto de salida hacia devices (Auth Service por HTTP). */
public interface DevicesPort {

    /** Tokens push de todos los dispositivos de la persona (users.id). */
    List<String> tokensDe(Long userId);
}

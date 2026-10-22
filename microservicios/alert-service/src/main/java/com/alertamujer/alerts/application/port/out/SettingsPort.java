package com.alertamujer.alerts.application.port.out;

/** Puerto de salida: la configuración que define la administración (Auth Service). */
public interface SettingsPort {

    /** Minutos tras los cuales una alerta activa se marca como expirada (0 = sin configuración). */
    int maxAlertDurationMinutes();

    int maxContacts();
}

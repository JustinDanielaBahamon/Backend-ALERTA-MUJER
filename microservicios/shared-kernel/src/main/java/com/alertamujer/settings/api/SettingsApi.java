package com.alertamujer.settings.api;

/** Parámetros del sistema que necesitan otros módulos. */
public interface SettingsApi {

    /** Minutos tras los cuales una alerta activa se marca como expirada (0 = sin configuración). */
    int duracionMaximaAlertaMinutos();

    /** Número de emergencia que se ofrece a las usuarias y sus contactos (en Colombia, 123). */
    String numeroEmergencia();

    /** Máximo de contactos de emergencia por usuaria (5 si no hay configuración). */
    int maximoContactos();
}

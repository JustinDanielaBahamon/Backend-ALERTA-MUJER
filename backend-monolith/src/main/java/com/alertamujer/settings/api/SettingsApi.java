package com.alertamujer.settings.api;

/** Parámetros del sistema que necesitan otros módulos. */
public interface SettingsApi {

    /** Minutos tras los cuales una alerta activa se marca como expirada (0 = sin configuración). */
    int duracionMaximaAlertaMinutos();

    /** Máximo de contactos de emergencia por usuaria (5 si no hay configuración). */
    int maximoContactos();
}

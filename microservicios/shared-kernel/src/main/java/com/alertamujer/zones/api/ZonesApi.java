package com.alertamujer.zones.api;

/** Consultas del módulo zones para los demás módulos. */
public interface ZonesApi {

    /** Zonas activas en el mapa (estadísticas del panel). */
    long countActiveZones();
}

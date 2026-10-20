package com.alertamujer.alerts.application.port.in;

import java.time.LocalDateTime;

/**
 * Limpia el historial de ubicaciones: lo del día a día se guarda poco; los recorridos de una alerta,
 * más tiempo (pueden servir como prueba). La usuaria ve estos plazos en la app y en la web.
 */
public interface LimpiarUbicacionesUseCase {

    /** Devuelve cuántas ubicaciones se borraron. */
    int limpiar(LocalDateTime ahora);
}

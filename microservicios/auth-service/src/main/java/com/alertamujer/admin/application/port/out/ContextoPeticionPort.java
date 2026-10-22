package com.alertamujer.admin.application.port.out;

/** Puerto de salida: datos de la petición en curso (vacíos si no hay petición, p. ej. una tarea automática). */
public interface ContextoPeticionPort {

    String ip();

    /** Navegador o app y sistema del cliente. */
    String device();

    /** "Panel web · Chrome · Linux", "App móvil · Android"... */
    String source();
}

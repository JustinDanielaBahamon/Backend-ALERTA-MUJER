package com.alertamujer.admin.application.port.out;

/** Output port: data of the current request (empty if there is no request, e.g. a scheduled task). */
public interface RequestContextPort {

    String ip();

    /** Navegador o app y sistema del cliente. */
    String device();

    /** "Panel web · Chrome · Linux", "App móvil · Android"... */
    String source();
}

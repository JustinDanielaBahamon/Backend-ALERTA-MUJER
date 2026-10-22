package com.alertamujer.admin.application.port.out;

import java.time.LocalDateTime;

/** Puerto de salida: estado de las piezas técnicas del servidor. */
public interface InfrastructurePort {

    boolean databaseResponds();

    /** Firebase configurado: sin él no salen notificaciones push. */
    boolean pushConfigured();

    LocalDateTime startup();

    int versionJava();
}

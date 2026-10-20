package com.alertamujer.admin.application.port.out;

import java.time.LocalDateTime;

/** Puerto de salida: estado de las piezas técnicas del servidor. */
public interface InfraestructuraPort {

    boolean baseDeDatosResponde();

    /** Firebase configurado: sin él no salen notificaciones push. */
    boolean pushConfigurado();

    LocalDateTime arranque();

    int versionJava();
}

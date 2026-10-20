package com.alertamujer.alerts.application.port.out;

/** Puerto de salida: borrado de los datos personales de una usuaria en Alert Service. */
public interface DatosDeUsuariaRepositorio {

    /** Devuelve cuántas filas se borraron o limpiaron. */
    int borrarDe(Long userProfileId);
}

package com.alertamujer.alerts.application.port.out;

/** Puerto de salida: borrado de los datos personales de una usuaria en Alert Service. */
public interface UserDataRepository {

    /** Devuelve cuántas filas se borraron o limpiaron. */
    int deleteOf(Long userProfileId);
}

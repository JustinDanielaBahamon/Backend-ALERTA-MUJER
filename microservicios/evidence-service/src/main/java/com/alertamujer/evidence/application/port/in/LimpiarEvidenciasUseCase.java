package com.alertamujer.evidence.application.port.in;

import java.time.LocalDateTime;

/**
 * Caso de uso: borrar las evidencias vencidas (60 días) salvo las de alertas conservadas, y todas las
 * de una usuaria que eliminó su cuenta.
 */
public interface LimpiarEvidenciasUseCase {

    /** Devuelve cuántas se borraron. */
    int limpiar(LocalDateTime ahora);

    /** Cuenta eliminada: borra todas sus evidencias, aunque las hubiera marcado para conservar. */
    int borrarDeUsuaria(Long userProfileId);
}

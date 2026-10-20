package com.alertamujer.evidence.application.port.in;

import java.time.LocalDateTime;

/** Caso de uso: borrar las evidencias vencidas (60 días) salvo las de alertas conservadas. */
public interface LimpiarEvidenciasUseCase {

    /** Devuelve cuántas se borraron. */
    int limpiar(LocalDateTime ahora);
}

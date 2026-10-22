package com.alertamujer.alerts.application.port.in;

import java.time.LocalDateTime;

/**
 * Marca como "expired" las alertas que siguen activas más allá de la duración máxima configurada
 * por la administración, para que no queden abiertas para siempre.
 */
public interface ExpirarAlertasUseCase {

    /** Revisión automática: expira las vencidas y recuerda el resultado para el panel. */
    int revisar(LocalDateTime now);

    /** Última revisión automática (null si aún no corrió). */
    LocalDateTime ultimaRevision();

    int expiradasUltimaRevision();
}

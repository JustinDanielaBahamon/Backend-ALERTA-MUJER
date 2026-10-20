package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alertas que recibió la usuaria como contacto de emergencia de otra persona (se le avisó a su
 * correo). Desde el panel web ve cuáles están activas y abre su seguimiento.
 */
public interface AlertasRecibidasUseCase {

    /** Las de los últimos 30 días, de la más reciente a la más antigua. */
    List<ReceivedAlert> recibidas(Long userId, LocalDateTime ahora);

    /** Código de seguimiento de una alerta recibida, solo si el seguimiento sigue vigente. */
    String enlaceSeguimiento(Long userId, Long alertId, LocalDateTime ahora);

    /** Lo que ve el contacto: solo el nombre de pila de quien activó la alerta. */
    record ReceivedAlert(Long alertId, String firstName, String status, boolean active,
                         LocalDateTime startedAt, LocalDateTime endedAt, boolean trackingAvailable) {}
}

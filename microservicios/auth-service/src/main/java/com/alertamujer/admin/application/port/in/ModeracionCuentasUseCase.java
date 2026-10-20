package com.alertamujer.admin.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bloqueos y eliminaciones de cuentas. Bloquear exige un motivo (y opcionalmente una captura) y
 * queda como un caso cerrado en Moderación. Eliminar es irreversible: una administradora lo
 * solicita y otra distinta lo aprueba.
 */
public interface ModeracionCuentasUseCase {

    void bloquear(Long userId, Bloqueo bloqueo, Long adminId);

    void desbloquear(Long userId, String nota, Long adminId);

    /** Lo que se ha hecho con la cuenta, del más reciente al más antiguo. */
    List<HistorialItem> historial(Long userId);

    SolicitudView solicitarEliminacion(Long userId, String motivo, Long adminId);

    List<SolicitudView> solicitudes();

    SolicitudView aprobar(Long solicitudId, String nota, Long adminId);

    SolicitudView rechazar(Long solicitudId, String nota, Long adminId);

    record Bloqueo(String reason, String description, String evidence) {}

    record HistorialItem(LocalDateTime fecha, String tipo, String adminNombre, String motivo, String nota,
                         Long casoId, boolean tieneEvidencia) {}

    record SolicitudView(Long id, Long userId, String userName, String userEmail, Long requestedBy,
                         String requestedByName, String reason, String status, Long decidedBy,
                         String decidedByName, String decisionNote, LocalDateTime createdAt,
                         LocalDateTime decidedAt) {}
}

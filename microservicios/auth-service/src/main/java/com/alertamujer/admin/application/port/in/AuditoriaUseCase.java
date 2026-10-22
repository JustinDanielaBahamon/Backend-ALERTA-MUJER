package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.AuditFilter;

import java.time.LocalDateTime;
import java.util.List;

/** Auditoría del panel: cambios registrados por los triggers y eventos de sesión de las cuentas admin. */
public interface AuditoriaUseCase {

    AuditPage listAll(AuditFilter filter, int page, int size);

    /** CSV (";" y BOM UTF-8, para Excel en español) con los registros que cumplen el filtro. */
    byte[] export(AuditFilter filter);

    /** Cuentas de administración, para filtrar por responsable. */
    List<AuditActor> actors();

    record AuditPage(List<AuditLogView> items, long total, int page, int size) {}

    record AuditActor(Long accountId, String name, String email) {}

    /**
     * actorName es null si no hubo una sesión detrás del cambio; source dice entonces de dónde vino
     * (p. ej. una tarea automática). Ambos null = registro anterior a guardar el origen o SQL directo.
     */
    record AuditLogView(Long id, String action, String entityType, Integer entityId, String description,
                        Long actorAccountId, String actorName, String actorEmail, String actorRole,
                        String source, String ipAddress, LocalDateTime createdAt) {}
}

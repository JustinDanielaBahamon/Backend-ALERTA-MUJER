package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.AuditFilter;

import java.util.List;

/** Puerto de salida: el registro de auditoría (lo llenan los triggers y los eventos de sesión). */
public interface AuditRepository {

    /** Página de registros que cumplen el filtro, del más reciente al más antiguo. */
    AuditLogPage page(AuditFilter filter, int page, int size);

    /** Los primeros registros que cumplen el filtro (para exportar). */
    List<AuditLog> first(AuditFilter filter, int max);

    AuditLog save(AuditLog record);

    record AuditLogPage(List<AuditLog> records, long total, int number) {}
}

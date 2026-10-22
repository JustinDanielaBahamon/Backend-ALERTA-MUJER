package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.FiltroAuditoria;

import java.util.List;

/** Puerto de salida: el registro de auditoría (lo llenan los triggers y los eventos de sesión). */
public interface AuditoriaRepositorio {

    /** Página de registros que cumplen el filtro, del más reciente al más antiguo. */
    Pagina pagina(FiltroAuditoria filtro, int pagina, int size);

    /** Los primeros registros que cumplen el filtro (para exportar). */
    List<AuditLog> primeros(FiltroAuditoria filtro, int maximo);

    AuditLog save(AuditLog registro);

    record Pagina(List<AuditLog> registros, long total, int numero) {}
}

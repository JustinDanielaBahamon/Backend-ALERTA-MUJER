package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.AuditFilter;

import java.util.List;

/** Output port: the audit log (filled by the triggers and the session events). */
public interface AuditRepository {

    /** Page of records matching the filter, newest to oldest. */
    AuditLogPage page(AuditFilter filter, int page, int size);

    /** The first records matching the filter (for export). */
    List<AuditLog> first(AuditFilter filter, int max);

    AuditLog save(AuditLog record);

    record AuditLogPage(List<AuditLog> records, long total, int number) {}
}

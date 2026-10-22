package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.AuditFilter;

import java.time.LocalDateTime;
import java.util.List;

/** Panel audit: changes recorded by the triggers and session events of the admin accounts. */
public interface AuditUseCase {

    AuditPage listAll(AuditFilter filter, int page, int size);

    /** CSV (";" and UTF-8 BOM, for Excel in Spanish) with the records matching the filter. */
    byte[] export(AuditFilter filter);

    /** Admin accounts, to filter by actor. */
    List<AuditActor> actors();

    record AuditPage(List<AuditLogView> items, long total, int page, int size) {}

    record AuditActor(Long accountId, String name, String email) {}

    /**
     * actorName is null if there was no session behind the change; source then says where it came from
     * (e.g. a scheduled task). Both null = record older than storing the source, or direct SQL.
     */
    record AuditLogView(Long id, String action, String entityType, Integer entityId, String description,
                        Long actorAccountId, String actorName, String actorEmail, String actorRole,
                        String source, String ipAddress, LocalDateTime createdAt) {}
}

package com.alertamujer.admin.infrastructure.repository;

import com.alertamujer.admin.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByAccountId(Long accountId);
    List<AuditLog> findByEntityType(String entityType);
    List<AuditLog> findByAction(String action);
}

package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.ReportChanges;

import java.time.LocalDateTime;
import java.util.List;

/** Casos de moderación, con el nombre y correo de la usuaria reportada y de quien reporta. */
public interface UserReportsUseCase {

    /** Todos (estado null) o los de un estado, del más reciente al más antiguo. */
    List<UserReportView> listAll(String status);

    UserReportView get(Long id);

    /** Reporte de la administración sobre una usuaria identificada por su correo. */
    UserReportView create(String reportedEmail, String reason, String description, String severity, Long adminId);

    UserReportView update(Long id, ReportChanges changes, Long adminId);

    void delete(Long id);

    /** Captura adjunta al caso (data URI) o null. */
    String evidence(Long id);

    record UserReportView(Long id, Long reportedUserId, String reportedUserName, String reportedUserEmail,
                          Long reporterUserId, String reporterName, String reason, String description,
                          String severity, String status, String resolutionNotes, Integer reviewedByAdminId,
                          LocalDateTime reviewedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                          boolean hasEvidence) {}
}

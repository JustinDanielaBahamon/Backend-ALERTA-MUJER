package com.alertamujer.admin.application.port.in;

import com.alertamujer.admin.domain.model.ReportChanges;

import java.time.LocalDateTime;
import java.util.List;

/** Moderation cases, with the name and email of the reported user and of the reporter. */
public interface UserReportsUseCase {

    /** All (null status) or those with a status, newest to oldest. */
    List<UserReportView> listAll(String status);

    UserReportView get(Long id);

    /** Report by the administration about a user identified by her email. */
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

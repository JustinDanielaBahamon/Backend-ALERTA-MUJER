package com.alertamujer.admin.application.port.in;


import java.time.LocalDateTime;
import java.util.List;

/**
 * Account blocks and deletions. Blocking requires a reason (and optionally a screenshot) and
 * stays as a closed case in Moderation. Deleting is irreversible: one admin
 * requests it and a different one approves it.
 */
public interface AccountModerationUseCase {

    void block(Long userId, Block block, Long adminId);

    void unblock(Long userId, String note, Long adminId);

    /** What has been done with the account, newest to oldest. */
    List<HistoryItem> history(Long userId);

    DeletionRequestView requestDeletion(Long userId, String reason, Long adminId);

    List<DeletionRequestView> deletionRequests();

    DeletionRequestView approve(Long requestId, String note, Long adminId);

    DeletionRequestView reject(Long requestId, String note, Long adminId);

    record Block(String reason, String description, String evidence) {}

    record HistoryItem(LocalDateTime date, String type, String adminName, String reason, String note,
                         Long caseId, boolean hasEvidence) {}

    record DeletionRequestView(Long id, Long userId, String userName, String userEmail, Long requestedBy,
                         String requestedByName, String reason, String status, Long decidedBy,
                         String decidedByName, String decisionNote, LocalDateTime createdAt,
                         LocalDateTime decidedAt) {}
}

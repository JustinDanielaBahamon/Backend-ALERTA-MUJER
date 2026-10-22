package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.AccountModerationUseCase;
import com.alertamujer.admin.application.port.out.ModerationActionRepository;
import com.alertamujer.admin.application.port.out.AccountsPort;
import com.alertamujer.admin.application.port.out.UserReportRepository;
import com.alertamujer.admin.application.port.out.DeletionRequestRepository;
import com.alertamujer.admin.application.port.out.UsersPort;
import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import com.alertamujer.admin.domain.model.ModerationAction;
import com.alertamujer.admin.domain.model.ModerationText;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.shared.web.PhotoDataUri;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Bloqueos con motivo y eliminación de cuentas con aprobación de una segunda administradora. */
@Service
public class AccountModerationService implements AccountModerationUseCase {

    private static final String MIN = " (mínimo " + ModerationText.MIN_EXPLANATION + " caracteres)";

    private final UserReportRepository cases;
    private final ModerationActionRepository actions;
    private final DeletionRequestRepository deletionRequests;
    private final AccountsPort accounts;
    private final UsersPort users;

    public AccountModerationService(UserReportRepository cases, ModerationActionRepository actions,
                                    DeletionRequestRepository deletionRequests, AccountsPort accounts, UsersPort users) {
        this.cases = cases;
        this.actions = actions;
        this.deletionRequests = deletionRequests;
        this.accounts = accounts;
        this.users = users;
    }

    // ── Bloqueo ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void block(Long userId, Block block, Long adminId) {
        String reason = ModerationText.required(block == null ? null : block.reason(), "Elige el motivo del bloqueo", 100);
        String detail = ModerationText.explanation(block.description(), "Explica por qué se bloquea la cuenta" + MIN);
        String evidence = PhotoDataUri.validate(block.evidence());
        existing(userId);
        // identity impide que una administradora se bloquee a sí misma
        accounts.update(userId, new UserAccountChanges("blocked", null, null, null, null), adminId);
        UserReport reportCase = cases.save(UserReport.block(userId, adminId, reason, detail, evidence, LocalDateTime.now()));
        actions.save(ModerationAction.of(reportCase.getId(), adminId, ModerationAction.BLOCK, detail));
    }

    @Override
    @Transactional
    public void unblock(Long userId, String note, Long adminId) {
        String detail = ModerationText.explanation(note, "Explica por qué se desbloquea la cuenta" + MIN);
        existing(userId);
        accounts.update(userId, new UserAccountChanges("active", null, null, null, null), adminId);
        // Se anota en el último caso de la usuaria; si no tiene (bloqueo anterior a este registro), se crea uno
        UserReport reportCase = cases.ofUser(userId).stream()
                .max(Comparator.comparing(UserReport::getCreatedAt))
                .orElseGet(() -> cases.save(UserReport.unblockWithoutCase(userId, adminId, detail, LocalDateTime.now())));
        actions.save(ModerationAction.of(reportCase.getId(), adminId, ModerationAction.UNBLOCK, detail));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryItem> history(Long userId) {
        Map<Long, UserReport> byId = cases.ofUser(userId).stream().collect(Collectors.toMap(UserReport::getId, r -> r));
        Map<Long, UserSummary> names = users.byId();
        Stream<HistoryItem> fromActions = byId.isEmpty() ? Stream.empty()
                : actions.fromCases(byId.keySet()).stream().map(a -> {
                    UserReport reportCase = byId.get(a.getUserReportId());
                    return new HistoryItem(a.getCreatedAt(), a.getActionType(), name(names.get(a.getAdminProfileId().longValue())),
                            a.isBlock() ? reportCase.getReason() : null, a.getNotes(), reportCase.getId(), reportCase.hasEvidence());
                });
        Stream<HistoryItem> fromRequests = deletionRequests.all().stream()
                .filter(s -> s.getUserId().equals(userId))
                .map(s -> new HistoryItem(s.getCreatedAt(), "deletion_" + s.getStatus(), name(names.get(s.getRequestedBy())),
                        null, s.getReason(), null, false));
        return Stream.concat(fromActions, fromRequests).sorted(Comparator.comparing(HistoryItem::date).reversed()).toList();
    }

    // ── Eliminación con dos administradoras ────────────────────────

    @Override
    @Transactional
    public DeletionRequestView requestDeletion(Long userId, String reason, Long adminId) {
        String detail = ModerationText.explanation(reason, "Explica por qué se elimina la cuenta" + MIN);
        existing(userId);
        AccountDeletionRequest newOne = AccountDeletionRequest.newOne(userId, adminId, detail);
        if (deletionRequests.hasPending(userId)) throw new IllegalArgumentException("Ya hay una solicitud pendiente para esta cuenta");
        return view(deletionRequests.save(newOne), users.byId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeletionRequestView> deletionRequests() {
        Map<Long, UserSummary> names = users.byId();
        return deletionRequests.all().stream().map(s -> view(s, names)).toList();
    }

    @Override
    @Transactional
    public DeletionRequestView approve(Long requestId, String note, Long adminId) {
        AccountDeletionRequest s = existingRequest(requestId);
        s.approve(adminId, note, LocalDateTime.now());
        accounts.anonymize(s.getUserId());
        return view(deletionRequests.save(s), users.byId());
    }

    @Override
    @Transactional
    public DeletionRequestView reject(Long requestId, String note, Long adminId) {
        AccountDeletionRequest s = existingRequest(requestId);
        s.reject(adminId, note, LocalDateTime.now());
        return view(deletionRequests.save(s), users.byId());
    }

    // ── Apoyo ──────────────────────────────────────────────────────

    private void existing(Long userId) {
        users.find(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private AccountDeletionRequest existingRequest(Long id) {
        return deletionRequests.find(id).orElseThrow(() -> new ResourceNotFoundException("AccountDeletionRequest", id));
    }

    private static String name(UserSummary u) {
        return u == null ? null : u.fullName();
    }

    private static DeletionRequestView view(AccountDeletionRequest s, Map<Long, UserSummary> names) {
        UserSummary account = names.get(s.getUserId());
        return new DeletionRequestView(s.getId(), s.getUserId(), name(account), account != null ? account.email() : null,
                s.getRequestedBy(), name(names.get(s.getRequestedBy())), s.getReason(), s.getStatus(),
                s.getDecidedBy(), s.getDecidedBy() == null ? null : name(names.get(s.getDecidedBy())),
                s.getDecisionNote(), s.getCreatedAt(), s.getDecidedAt());
    }
}

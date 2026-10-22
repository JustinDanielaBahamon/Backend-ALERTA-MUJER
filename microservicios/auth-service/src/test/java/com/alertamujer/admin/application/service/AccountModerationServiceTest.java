package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.AccountModerationUseCase.Block;
import com.alertamujer.admin.application.port.in.AccountModerationUseCase.HistoryItem;
import com.alertamujer.admin.application.port.out.*;
import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import com.alertamujer.admin.domain.model.ModerationAction;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Moderación de cuentas con los puertos simulados. */
class AccountModerationServiceTest {

    private final UserReportRepository cases = mock(UserReportRepository.class);
    private final ModerationActionRepository actions = mock(ModerationActionRepository.class);
    private final DeletionRequestRepository deletionRequests = mock(DeletionRequestRepository.class);
    private final AccountsPort accounts = mock(AccountsPort.class);
    private final UsersPort users = mock(UsersPort.class);
    private final AccountModerationService service = new AccountModerationService(cases, actions, deletionRequests, accounts, users);

    private static final UserSummary ANA = new UserSummary(5L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
    private static final UserSummary ADMIN = new UserSummary(2L, "Sara", "Ruiz", "sara@correo.com", null, UserSummary.ADMIN_ROLE, null);

    @BeforeEach
    void setUp() {
        when(users.find(5L)).thenReturn(Optional.of(ANA));
        when(users.byId()).thenReturn(Map.of(5L, ANA, 2L, ADMIN));
        when(cases.save(any())).thenAnswer(inv -> {
            UserReport r = inv.getArgument(0);
            r.setId(40L);
            return r;
        });
        when(deletionRequests.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void blockChangesTheAccountAndLeavesACaseWithItsAction() {
        service.block(5L, new Block("Acoso", "Mensajes ofensivos reiterados", null), 2L);

        verify(accounts).update(5L, new UserAccountChanges("blocked", null, null, null, null), 2L);
        ArgumentCaptor<ModerationAction> action = ArgumentCaptor.forClass(ModerationAction.class);
        verify(actions).save(action.capture());
        assertEquals(40L, action.getValue().getUserReportId());
        assertEquals("block", action.getValue().getActionType());
    }

    @Test
    void blockValidatesBeforeTouchingTheAccount() {
        assertThrows(IllegalArgumentException.class, () -> service.block(5L, new Block(" ", "Mensajes ofensivos", null), 2L));
        assertThrows(IllegalArgumentException.class, () -> service.block(5L, new Block("Acoso", "Corto", null), 2L));
        assertThrows(IllegalArgumentException.class, () -> service.block(5L, new Block("Acoso", "Mensajes ofensivos", "<script>"), 2L));
        assertThrows(ResourceNotFoundException.class, () -> service.block(9L, new Block("Acoso", "Mensajes ofensivos", null), 2L));
        verifyNoInteractions(accounts);
    }

    @Test
    void unblockNotesTheLastCaseOrCreatesOne() {
        when(cases.ofUser(5L)).thenReturn(List.of());
        service.unblock(5L, "Se revisó el caso con ella", 2L);
        verify(cases).save(argThat(r -> "Desbloqueo".equals(r.getReason())));
        verify(accounts).update(5L, new UserAccountChanges("active", null, null, null, null), 2L);
    }

    @Test
    void theHistoryMergesActionsAndRequestsNewestFirst() {
        UserReport reportCase = UserReport.block(5L, 2L, "Acoso", "Mensajes", null, LocalDateTime.now());
        reportCase.setId(40L);
        ModerationAction block = ModerationAction.of(40L, 2L, "block", "Mensajes");
        block.setCreatedAt(LocalDateTime.now().minusDays(1));
        AccountDeletionRequest requested = AccountDeletionRequest.newOne(5L, 2L, "Cuenta duplicada");
        when(cases.ofUser(5L)).thenReturn(List.of(reportCase));
        when(actions.fromCases(any())).thenReturn(List.of(block));
        when(deletionRequests.all()).thenReturn(List.of(requested));

        List<HistoryItem> h = service.history(5L);

        assertEquals(List.of("deletion_pending", "block"), h.stream().map(HistoryItem::type).toList());
        assertEquals("Acoso", h.get(1).reason());
        assertEquals("Sara Ruiz", h.get(1).adminName());
    }

    @Test
    void cannotBeRequestedTwiceOrApprovedByTheSameAdmin() {
        when(deletionRequests.hasPending(5L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.requestDeletion(5L, "Cuenta duplicada", 2L));

        AccountDeletionRequest s = AccountDeletionRequest.newOne(5L, 2L, "Cuenta duplicada");
        when(deletionRequests.find(7L)).thenReturn(Optional.of(s));
        assertThrows(IllegalArgumentException.class, () -> service.approve(7L, null, 2L));
        verify(accounts, never()).anonymize(any());

        assertEquals("approved", service.approve(7L, "Confirmado", 3L).status());
        verify(accounts).anonymize(5L);
    }
}

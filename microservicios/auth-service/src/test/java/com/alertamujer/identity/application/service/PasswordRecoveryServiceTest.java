package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import com.alertamujer.identity.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock private UserRepository users;
    @Mock private AccountRepository accounts;
    @Mock private RecoveryRepository deletionRequests;
    @Mock private HashingPort hashing;
    @Mock private IdentityEventsPort events;

    @InjectMocks private PasswordRecoveryService service;

    private User ana;
    private Account account;
    private final List<RecoveryRequest> pendingOnes = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ana = User.newOne("Ana Gómez", "ana@correo.com", null, null, null, null);
        ana.setId(1L);
        account = Account.newOne(1L, "hash");
        account.setId(4L);
        lenient().when(users.byEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        lenient().when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
        lenient().when(accounts.find(4L)).thenReturn(Optional.of(account));
        lenient().when(deletionRequests.pendingForAccount(4L)).thenAnswer(inv -> List.copyOf(pendingOnes));
    }

    private RecoveryRequest pending(String code, LocalDateTime created) {
        RecoveryRequest r = RecoveryRequest.newOne(4L, code, null, created);
        pendingOnes.add(r);
        return r;
    }

    @Test
    void sendsASixDigitCodeAndStoresOnlyItsHash() {
        service.request(" ana@correo.com ", "1.2.3.4");

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(events).recoveryCode(eq("ana@correo.com"), eq("Ana Gómez"), code.capture(), eq(15));
        assertTrue(code.getValue().matches("\\d{6}"));
        ArgumentCaptor<RecoveryRequest> saved = ArgumentCaptor.forClass(RecoveryRequest.class);
        verify(deletionRequests).save(saved.capture());
        assertEquals(RecoveryRequest.hash(4L, code.getValue()), saved.getValue().getTokenHash());
        assertEquals("1.2.3.4", saved.getValue().getRequestedIp());
    }

    @Test
    void unregisteredEmailOrBlockedAccountRevealsNothing() {
        when(users.byEmail("nadie@correo.com")).thenReturn(Optional.empty());
        service.request("nadie@correo.com", null);

        account.changeStatus("blocked");
        service.request("ana@correo.com", null);

        verifyNoInteractions(events);
        verify(deletionRequests, never()).save(any());
    }

    @Test
    void doesNotSendAnotherCodeWithinAMinuteAndTheNewOneReplacesTheOld() {
        RecoveryRequest recent = pending("111111", LocalDateTime.now().minusSeconds(10));
        service.request("ana@correo.com", null);
        verifyNoInteractions(events);

        recent.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        service.request("ana@correo.com", null);
        assertEquals("replaced", recent.getStatus());
        verify(events).recoveryCode(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void withTheRightCodeChangesThePasswordAndMarksItUsed() {
        RecoveryRequest r = pending("123456", LocalDateTime.now());
        when(hashing.hash("NuevaClave1")).thenReturn("hash2");

        service.reset("ana@correo.com", "123456", "NuevaClave1");

        assertEquals("hash2", account.getPasswordHash());
        assertEquals("used", r.getStatus());
        assertNotNull(r.getUsedAt());
    }

    @Test
    void fiveFailedAttemptsCancelTheCode() {
        RecoveryRequest r = pending("123456", LocalDateTime.now());

        for (int i = 0; i < RecoveryRequest.MAX_ATTEMPTS; i++) {
            assertThrows(IllegalArgumentException.class, () -> service.verify("ana@correo.com", "000000"));
        }

        assertEquals("expired", r.getStatus());
        assertEquals(5L, r.getAttempts());
    }

    @Test
    void anExpiredOrMalformedCodeDoesNotWork() {
        RecoveryRequest r = pending("123456", LocalDateTime.now().minusMinutes(20));

        assertThrows(IllegalArgumentException.class, () -> service.verify("ana@correo.com", "12ab56"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.verify("ana@correo.com", "123456"));

        assertEquals(PasswordRecoveryService.INVALID_CODE, e.getMessage());
        assertEquals("expired", r.getStatus());
    }
}

package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.in.ManageAccountsUseCase.UserAccount;
import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.AccountChanges;
import com.alertamujer.identity.domain.model.DeviceSession;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountAdministrationServiceTest {

    @Mock private UserRepository users;
    @Mock private AccountRepository accounts;
    @Mock private ProfileRepository profiles;
    @Mock private SessionRepository sessions;
    @Mock private HashingPort hashing;
    @Mock private IdentityEventsPort events;

    @InjectMocks private AccountAdministrationService service;

    private User ana;
    private Account account;

    @BeforeEach
    void setUp() {
        ana = User.newOne("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", null);
        ana.setId(1L);
        account = Account.newOne(1L, "hash");
        account.setId(4L);
        lenient().when(users.find(1L)).thenReturn(Optional.of(ana));
        lenient().when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
    }

    private static AccountChanges status(String status) {
        return new AccountChanges(status, null, null, null, null);
    }

    @Test
    void changesTheAccountStatusAndTheData() {
        UserAccount r = service.update(1L, new AccountChanges("blocked", "Ana María", null, null, null), 2L);

        assertEquals("blocked", r.account().getStatus());
        assertEquals("Ana María", r.user().getFirstName());
        assertEquals("Gómez", r.user().getLastName());
        verify(accounts).save(account);
        verify(users).save(ana);
    }

    @Test
    void cannotBlockHerselfOrUseUnknownStatuses() {
        IllegalArgumentException own = assertThrows(IllegalArgumentException.class, () -> service.update(1L, status("blocked"), 1L));
        assertEquals("Cannot block your own account", own.getMessage());
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, status("raro"), 2L));
        verify(accounts, never()).save(any());
    }

    @Test
    void doesNotAllowARepeatedEmail() {
        User other = new User();
        other.setId(5L);
        when(users.byEmail("eva@correo.com")).thenReturn(Optional.of(other));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, new AccountChanges(null, null, null, null, "eva@correo.com"), 2L));
        assertEquals("Email already in use", e.getMessage());
        verify(users, never()).save(any());
    }

    @Test
    void anonymizeErasesPersonalDataAndClosesSessions() {
        UserProfile profile = UserProfile.of(1L);
        profile.setId(10L);
        profile.setProfilePhotoUrl("data:image/jpeg;base64,/9j/4AAQ");
        when(profiles.ofUser(1L)).thenReturn(Optional.of(profile));
        DeviceSession session = new DeviceSession();
        when(sessions.activeForAccount(4L)).thenReturn(List.of(session));
        when(hashing.hash(anyString())).thenReturn("inutilizable");

        service.anonymize(1L);

        assertEquals("eliminada-1@alertamujer.invalid", ana.getEmail());
        assertNull(profile.getProfilePhotoUrl());
        assertEquals("inactive", account.getStatus());
        assertEquals("inutilizable", account.getPasswordHash());
        assertEquals("revoked", session.getStatus());
        verify(sessions).save(session);
        // Alert y Evidence borran contactos, ubicaciones y grabaciones del perfil
        verify(events).accountDeleted(1L, 10L);
    }

    @Test
    void listJoinsEachPersonWithHerAccountAndProfile() {
        UserProfile profile = UserProfile.of(1L);
        profile.setId(10L);
        when(users.all()).thenReturn(List.of(ana));
        when(accounts.all()).thenReturn(List.of(account));
        when(profiles.all()).thenReturn(List.of(profile));

        UserAccount r = service.listAll().get(0);

        assertSame(account, r.account());
        assertEquals(10L, r.profileId());
    }
}

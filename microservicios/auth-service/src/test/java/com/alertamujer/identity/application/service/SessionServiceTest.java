package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.RegistrationData;
import com.alertamujer.identity.application.port.in.Session;
import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.application.port.out.GoogleVerifierPort.GoogleIdentity;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import com.alertamujer.shared.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Registration and session with mocked ports (no real database, BCrypt or JWT). */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock private UserRepository users;
    @Mock private AccountRepository accounts;
    @Mock private ProfileRepository profiles;
    @Mock private HashingPort hashing;
    @Mock private TokensPort tokens;
    @Mock private GoogleVerifierPort google;
    @Mock private IdentityEventsPort events;

    @InjectMocks private SessionService service;

    private User ana;
    private Account account;

    @BeforeEach
    void setUp() {
        ana = User.newOne("Ana Gómez", "ana@correo.com", null, null, null, null);
        ana.setId(1L);
        account = Account.newOne(1L, "hash");
        account.setId(4L);
        lenient().when(tokens.access(anyLong(), anyString())).thenReturn("acceso");
        lenient().when(tokens.refresh(anyLong())).thenReturn("renovacion");
    }

    private static RegistrationData record() {
        return record(true);
    }

    private static RegistrationData record(Boolean acceptsTerms) {
        return new RegistrationData("Ana Gómez", "ana@correo.com", "Secreta123", "300 123-4567", "CC", "123", "05/05/1995",
                acceptsTerms);
    }

    @Test
    void register_createsPersonAccountAndProfileAndOpensTheSession() {
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(hashing.hash("Secreta123")).thenReturn("hash");

        Session s = service.registrar(record());

        assertEquals("acceso", s.token());
        assertEquals("renovacion", s.refreshToken());
        assertEquals("3001234567", s.user().getTelephone());
        assertEquals(LocalDate.of(1995, 5, 5), s.user().getBirthdate());
        ArgumentCaptor<Account> c = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(c.capture());
        assertEquals("hash", c.getValue().getPasswordHash());
        assertEquals(1L, c.getValue().getUserId());
        verify(profiles).save(any(UserProfile.class));
        verify(users).ensureUserRole();
        assertNotNull(s.user().getTermsAcceptedAt());
        assertEquals(User.CURRENT_TERMS, s.user().getTermsVersion());
    }

    @Test
    void register_rejectsIfTermsAreNotAccepted() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(record(false)));
        verify(users, never()).save(any());
    }

    @Test
    void register_withoutTheCheckbox_doesNotFakeTheRecord() {
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(hashing.hash("Secreta123")).thenReturn("hash");

        Session s = service.registrar(record(null));

        assertNull(s.user().getTermsAcceptedAt());
        assertNull(s.user().getTermsVersion());
    }

    @Test
    void register_reportsAllRepeatedFieldsAtOnce() {
        when(users.emailExists("ana@correo.com")).thenReturn(true);
        when(users.phoneExists("3001234567")).thenReturn(true);
        when(users.documentExists("123")).thenReturn(true);

        DuplicateFieldsException e = assertThrows(DuplicateFieldsException.class, () -> service.registrar(record()));

        assertEquals(3, e.getFieldErrors().size());
        verify(users, never()).save(any());
    }

    @Test
    void withEmail_signsInAndRecordsTheAccess() {
        when(users.byEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
        when(hashing.coincide("Secreta123", "hash")).thenReturn(true);

        Session s = service.withEmail("ana@correo.com", "Secreta123");

        assertSame(ana, s.user());
        assertNotNull(account.getLastAccess());
        verify(tokens).access(1L, "user");
        verifyNoInteractions(events);
    }

    @Test
    void withEmail_rejectsUnknownEmailWrongPasswordOrBlockedAccount() {
        when(users.byEmail("nadie@correo.com")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> service.withEmail("nadie@correo.com", "x"));

        when(users.byEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
        when(hashing.coincide("mala", "hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> service.withEmail("ana@correo.com", "mala"));

        account.changeStatus("blocked");
        InvalidCredentialsException e = assertThrows(InvalidCredentialsException.class,
                () -> service.withEmail("ana@correo.com", "Secreta123"));
        assertEquals("Account blocked", e.getMessage());
        verify(accounts, never()).save(any());
    }

    @Test
    void withEmail_anAdminIsAudited() {
        ana.setRoleId(User.ADMIN_ROLE);
        when(users.byEmail("ana@correo.com")).thenReturn(Optional.of(ana));
        when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
        when(hashing.coincide("mala", "hash")).thenReturn(false);
        when(hashing.coincide("Secreta123", "hash")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> service.withEmail("ana@correo.com", "mala"));
        service.withEmail("ana@correo.com", "Secreta123");

        verify(events).adminSession(4L, AdminSessionEvent.LOGIN_FAILED, "Contraseña incorrecta");
        verify(events).adminSession(4L, AdminSessionEvent.LOGIN_SUCCESS, "Con correo y contraseña");
        verify(tokens).access(1L, "admin");
    }

    @Test
    void withGoogle_createsTheUserIfMissing() {
        when(google.verify("token")).thenReturn(new GoogleIdentity("eva@correo.com", "Eva Ruiz", "Eva", "Ruiz"));
        when(users.byEmail("eva@correo.com")).thenReturn(Optional.empty());
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(9L);
            return u;
        });
        when(hashing.hash(anyString())).thenReturn("aleatoria");
        Account newOne = Account.newOne(9L, "aleatoria");
        when(accounts.ofUser(9L)).thenReturn(Optional.of(newOne));

        Session s = service.conGoogle("token");

        assertEquals("Eva", s.user().getFirstName());
        assertEquals(User.CURRENT_TERMS, s.user().getTermsVersion());
        verify(profiles).save(any(UserProfile.class));
        verify(accounts, times(2)).save(any(Account.class));
    }

    @Test
    void refresh_givesANewTokenOnlyIfTheAccountIsStillActive() {
        when(tokens.userForRefresh("r")).thenReturn(1L);
        when(users.find(1L)).thenReturn(Optional.of(ana));
        when(accounts.ofUser(1L)).thenReturn(Optional.of(account));

        assertEquals("acceso", service.refresh("r"));

        account.changeStatus("blocked");
        InvalidCredentialsException e = assertThrows(InvalidCredentialsException.class, () -> service.refresh("r"));
        assertEquals("Refresh token inválido o expirado", e.getMessage());
    }

    @Test
    void refresh_anInvalidTokenGivesTheSameMessage() {
        when(tokens.userForRefresh("basura")).thenThrow(new IllegalArgumentException("firma"));

        InvalidCredentialsException e = assertThrows(InvalidCredentialsException.class, () -> service.refresh("basura"));
        assertEquals("Refresh token inválido o expirado", e.getMessage());
    }

    @Test
    void close_onlyAuditsAdmins() {
        when(users.find(1L)).thenReturn(Optional.of(ana));
        service.close(1L, "inactividad");
        service.close(null, null);
        verifyNoInteractions(events);

        ana.setRoleId(User.ADMIN_ROLE);
        when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
        service.close(1L, "inactividad");
        verify(events).adminSession(4L, AdminSessionEvent.LOGOUT, "Por inactividad");
    }
}

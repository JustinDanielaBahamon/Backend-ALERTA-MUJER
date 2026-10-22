package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.ManageAccountsUseCase;
import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.UserChanges;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyAccountServiceTest {

    @Mock private UserRepository users;
    @Mock private ProfileRepository profiles;
    @Mock private AccountRepository accounts;
    @Mock private HashingPort hashing;
    @Mock private IdentityEventsPort events;
    @Mock private ManageAccountsUseCase administration;

    @InjectMocks private MyAccountService service;

    private User ana;
    private UserProfile profile;
    private Account account;

    @BeforeEach
    void setUp() {
        ana = User.newOne("Ana Gómez", "ana@correo.com", null, null, null, null);
        ana.setId(1L);
        profile = UserProfile.of(1L);
        account = Account.newOne(1L, "hash");
        account.setId(4L);
        lenient().when(users.find(1L)).thenReturn(Optional.of(ana));
        lenient().when(profiles.ofUser(1L)).thenReturn(Optional.of(profile));
        lenient().when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static UserChanges photo(Optional<String> photo) {
        return new UserChanges(null, null, null, null, null, null, photo);
    }

    @Test
    void completeData_rejectsPhoneOrDocumentOfAnotherAccount() {
        when(users.phoneExists("3001234567")).thenReturn(true);
        when(users.documentExists("1080")).thenReturn(true);
        UserChanges changes = new UserChanges(null, null, Optional.of("300 123 4567"), Optional.of("1080"),
                Optional.of("C.C"), null, null);

        DuplicateFieldsException e = assertThrows(DuplicateFieldsException.class, () -> service.update(1L, changes));
        assertEquals(java.util.Set.of("telephone", "documentNumber"), e.getFieldErrors().keySet());
        verify(users, never()).save(any());
    }

    @Test
    void completeData_savesTheCleanPhone() {
        UserChanges changes = new UserChanges(null, null, Optional.of("300 123-4567"), Optional.of(" 1080 "),
                Optional.of("C.C"), java.time.LocalDate.of(1995, 5, 5), null);

        User saved = service.update(1L, changes).user();

        assertEquals("3001234567", saved.getTelephone());
        assertEquals("1080", saved.getDocumentNumber());
    }

    @Test
    void savesAndRemovesTheProfilePhoto() {
        assertEquals("data:image/jpeg;base64,/9j/4AAQ",
                service.update(1L, photo(Optional.of("data:image/jpeg;base64,/9j/4AAQ"))).photo());
        assertNull(service.update(1L, photo(Optional.of(""))).photo());
    }

    @Test
    void aPhotoThatIsNotAnImageSavesNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, photo(Optional.of("<script>"))));
        verify(users, never()).save(any());
        verify(profiles, never()).save(any());
    }

    @Test
    void changePassword_requiresTheCurrentOneAndANewDifferentOne() {
        when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
        when(hashing.coincide("mala", "hash")).thenReturn(false);
        when(hashing.coincide("Actual123", "hash")).thenReturn(true);
        when(hashing.coincide("Nueva1234", "hash")).thenReturn(false);
        when(hashing.hash("Nueva1234")).thenReturn("hash2");

        assertThrows(IllegalArgumentException.class, () -> service.changePassword(1L, "mala", "Nueva1234"));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(1L, "Actual123", "corta"));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(1L, "Actual123", "Actual123"));

        service.changePassword(1L, "Actual123", "Nueva1234");
        assertEquals("hash2", account.getPasswordHash());
        verify(accounts).save(account);
        verifyNoInteractions(events);
    }

    @Test
    void anAdminPasswordChangeIsAudited() {
        ana.setRoleId(User.ADMIN_ROLE);
        when(accounts.ofUser(1L)).thenReturn(Optional.of(account));
        when(hashing.coincide("Actual123", "hash")).thenReturn(true);
        when(hashing.coincide("Nueva1234", "hash")).thenReturn(false);
        when(hashing.hash("Nueva1234")).thenReturn("hash2");

        service.changePassword(1L, "Actual123", "Nueva1234");

        verify(events).adminSession(4L, AdminSessionEvent.PASSWORD_CHANGED, null);
    }

    @Test
    void deleteMyAccount_withTheConfirmationWord_usesTheSameDeletionAsTheAdministration() {
        service.deleteMyAccount(1L, " eliminar ");

        verify(administration).anonymize(1L);
    }

    @Test
    void deleteMyAccount_withoutConfirmationDeletesNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteMyAccount(1L, "si"));
        assertThrows(IllegalArgumentException.class, () -> service.deleteMyAccount(1L, null));
        verifyNoInteractions(administration);
    }

    @Test
    void deleteMyAccount_anAdminCannotDoItAlone() {
        ana.setRoleId(User.ADMIN_ROLE);

        assertThrows(IllegalArgumentException.class, () -> service.deleteMyAccount(1L, "ELIMINAR"));
        verifyNoInteractions(administration);
    }
}

package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Reglas de administración de cuentas (trasladadas desde AdminServiceTest al mover la lógica a identity). */
@ExtendWith(MockitoExtension.class)
class UserAccountAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private com.alertamujer.identity.infrastructure.repository.DeviceSessionRepository deviceSessionRepository;

    @InjectMocks
    private UserAccountAdminService service;

    @Test
    void updateUser_shouldUpdateAccountStatus() {
        // Given
        Long userId = 1L;
        Long currentUserId = 2L;
        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@test.com");

        Account account = new Account();
        account.setUserId(userId);
        account.setStatus("active");

        UserAccountChanges request = new UserAccountChanges("inactive", null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
                when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        UserAccountView result = service.updateUserAccount(userId, request, currentUserId);

        // Then
        verify(accountRepository).save(argThat(acc -> "inactive".equals(acc.getStatus())));
        assertNotNull(result);
    }

    @Test
    void updateUser_shouldPreventSelfBlocking() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setUserId(userId);
        account.setStatus("active");

        UserAccountChanges request = new UserAccountChanges("blocked", null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.updateUserAccount(userId, request, userId); // same user trying to block themselves
        });

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldRejectInvalidStatus() {
        // Given
        Long userId = 1L;
        Long currentUserId = 2L;
        User user = new User();
        user.setId(userId);

        Account account = new Account();
        account.setUserId(userId);

        UserAccountChanges request = new UserAccountChanges("invalid_status", null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.updateUserAccount(userId, request, currentUserId);
        });
    }

    @Test
    void updateUser_shouldUpdateUserFields() {
        // Given
        Long userId = 1L;
        Long currentUserId = 2L;
        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@test.com");

        UserAccountChanges request = new UserAccountChanges(
                null, "Jane", "Smith", "1234567890", "jane@test.com"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.empty());
                when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        UserAccountView result = service.updateUserAccount(userId, request, currentUserId);

        // Then
        verify(userRepository).save(argThat(u ->
                "Jane".equals(u.getFirstName()) &&
                        "Smith".equals(u.getLastName()) &&
                        "1234567890".equals(u.getTelephone()) &&
                        "jane@test.com".equals(u.getEmail())
        ));
        assertNotNull(result);
    }

    @Test
    void updateUser_shouldRejectDuplicateEmail() {
        // Given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long currentUserId = 3L;

        User user = new User();
        user.setId(userId);
        user.setEmail("user1@test.com");

        User otherUser = new User();
        otherUser.setId(otherUserId);
        otherUser.setEmail("user2@test.com");

        UserAccountChanges request = new UserAccountChanges(null, null, null, null, "user2@test.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(otherUser));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.updateUserAccount(userId, request, currentUserId);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void anonimizarBorraLosDatosPersonalesYCierraLasSesiones() {
        com.alertamujer.identity.domain.model.User user = new com.alertamujer.identity.domain.model.User();
        user.setId(5L);
        user.setFirstName("Ana");
        user.setLastName("Pérez");
        user.setEmail("ana@correo.com");
        user.setTelephone("3001234567");
        user.setDocumentNumber("123");
        com.alertamujer.identity.domain.model.Account cuenta = new com.alertamujer.identity.domain.model.Account();
        cuenta.setId(9L);
        cuenta.setUserId(5L);
        cuenta.setStatus("blocked");
        cuenta.setPasswordHash("hash-viejo");
        com.alertamujer.identity.domain.model.DeviceSession sesion = new com.alertamujer.identity.domain.model.DeviceSession();
        sesion.setStatus("active");
        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(user));
        when(userProfileRepository.findByUserId(5L)).thenReturn(java.util.Optional.empty());
        when(accountRepository.findByUserId(5L)).thenReturn(java.util.Optional.of(cuenta));
        when(deviceSessionRepository.findByAccountIdAndStatus(9L, "active")).thenReturn(java.util.List.of(sesion));

        service.anonymizeAccount(5L);

        assertEquals("eliminada-5@alertamujer.invalid", user.getEmail());
        assertNull(user.getTelephone());
        assertNull(user.getDocumentNumber());
        assertEquals("inactive", cuenta.getStatus());
        assertNotEquals("hash-viejo", cuenta.getPasswordHash());
        assertEquals("revoked", sesion.getStatus());
    }
}

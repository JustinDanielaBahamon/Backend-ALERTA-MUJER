package com.alertamujer.admin.application.service;

import com.alertamujer.admin.infrastructure.controller.dto.AdminUserDto;
import com.alertamujer.admin.infrastructure.controller.dto.UpdateUserRequest;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private com.alertamujer.identity.infrastructure.repository.UserProfileRepository userProfileRepository;

    @Mock
    private com.alertamujer.alerts.infrastructure.repository.AlertRepository alertRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(
                userRepository,
                accountRepository,
                userProfileRepository,
                alertRepository,
                null, null, null, null, null, null, null
        );
    }

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

        UpdateUserRequest request = new UpdateUserRequest("inactive", null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        AdminUserDto result = adminService.updateUser(userId, request, currentUserId);

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

        UpdateUserRequest request = new UpdateUserRequest("blocked", null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            adminService.updateUser(userId, request, userId); // same user trying to block themselves
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

        UpdateUserRequest request = new UpdateUserRequest("invalid_status", null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            adminService.updateUser(userId, request, currentUserId);
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

        UpdateUserRequest request = new UpdateUserRequest(
                null, "Jane", "Smith", "1234567890", "jane@test.com"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        AdminUserDto result = adminService.updateUser(userId, request, currentUserId);

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

        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "user2@test.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(otherUser));

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            adminService.updateUser(userId, request, currentUserId);
        });

        verify(userRepository, never()).save(any());
    }
}

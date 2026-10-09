package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.domain.model.Role;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AlertService alertService;

    private User testUser;
    private User adminUser;
    private UserProfile testUserProfile;
    private UserProfile otherUserProfile;
    private Alert testAlert;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("user");

        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("administrator");

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Ana");
        testUser.setLastName("García");
        testUser.setEmail("ana@test.com");
        testUser.setRole(userRole);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(adminRole);

        testUserProfile = new UserProfile();
        testUserProfile.setId(10L);
        testUserProfile.setUser(testUser);

        otherUserProfile = new UserProfile();
        otherUserProfile.setId(20L);

        testAlert = new Alert();
        testAlert.setId(1L);
        testAlert.setUserProfileId(10L);
        testAlert.setDeviceId(1L);
        testAlert.setAlertType("main");
        testAlert.setActivationMethod("panic_button");
        testAlert.setStatus("active");
        testAlert.setStartedAt(LocalDateTime.now());
    }

    @Test
    void testCreateAlert_AssignsUserFromJWT() {
        // Arrange
        Alert newAlert = new Alert();
        newAlert.setDeviceId(1L);
        newAlert.setAlertType("main");
        newAlert.setActivationMethod("panic_button");

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testUserProfile));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        Alert result = alertService.createAlert(newAlert, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getUserProfileId());
        verify(userProfileRepository, times(1)).findByUserId(1L);
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void testCreateAlert_ProfileNotFound_ThrowsException() {
        // Arrange
        Alert newAlert = new Alert();
        newAlert.setDeviceId(1L);

        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            alertService.createAlert(newAlert, 999L)
        );
        assertTrue(exception.getMessage().contains("UserProfile for user"));
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void testUpdateAlert_Success() {
        // Arrange
        Alert updateData = new Alert();
        updateData.setStatus("resolved");
        updateData.setMessage("Todo resuelto");

        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        // Act
        Alert result = alertService.updateAlert(1L, updateData, 10L);

        // Assert
        assertNotNull(result);
        assertEquals("resolved", result.getStatus());
        assertEquals("Todo resuelto", result.getMessage());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void testUpdateAlert_UnauthorizedOwner_ThrowsAccessDenied() {
        // Arrange
        Alert updateData = new Alert();
        updateData.setStatus("resolved");

        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
            alertService.updateAlert(1L, updateData, 999L)
        );
        assertEquals("No puedes modificar alertas de otro usuario", exception.getMessage());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void testDeleteAlert_Success() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        doNothing().when(alertRepository).deleteById(1L);

        // Act
        alertService.deleteAlert(1L, 10L);

        // Assert
        verify(alertRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAlert_UnauthorizedOwner_ThrowsAccessDenied() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
            alertService.deleteAlert(1L, 999L)
        );
        assertEquals("No puedes eliminar alertas de otro usuario", exception.getMessage());
        verify(alertRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAlertsByUserProfile_OwnerAccess() {
        // Arrange
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testUserProfile));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(alertRepository.findByUserProfileId(10L)).thenReturn(List.of(testAlert));

        // Act
        List<Alert> results = alertService.getAlertsByUserProfile(10L, 1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testAlert.getId(), results.get(0).getId());
    }

    @Test
    void testGetAlertsByUserProfile_UnauthorizedAccess_ThrowsAccessDenied() {
        // Arrange
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testUserProfile));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
            alertService.getAlertsByUserProfile(20L, 1L)
        );
        assertEquals("No puedes ver alertas de otro usuario", exception.getMessage());
        verify(alertRepository, never()).findByUserProfileId(anyLong());
    }

    @Test
    void testGetAlertsByUserProfile_AdminCanAccessOthers() {
        // Arrange
        UserProfile adminProfile = new UserProfile();
        adminProfile.setId(50L);
        adminProfile.setUser(adminUser);

        when(userProfileRepository.findByUserId(2L)).thenReturn(Optional.of(adminProfile));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(alertRepository.findByUserProfileId(10L)).thenReturn(List.of(testAlert));

        // Act
        List<Alert> results = alertService.getAlertsByUserProfile(10L, 2L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(alertRepository, times(1)).findByUserProfileId(10L);
    }

    @Test
    void testGetAlertById_WithOwnershipCheck() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));

        // Act
        Optional<Alert> result = alertService.getAlertById(1L, 10L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAlert.getId(), result.get().getId());
    }

    @Test
    void testGetAlertById_WrongOwner_ReturnsEmpty() {
        // Arrange
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));

        // Act
        Optional<Alert> result = alertService.getAlertById(1L, 999L);

        // Assert
        assertFalse(result.isPresent());
    }
}

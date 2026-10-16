package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.config.FcmService;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
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
    private IdentityApi identityApi;

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private AlertService alertService;

    private UserSummary testUser;
    private UserSummary adminUser;
    private Long testUserProfile;
    private Long otherUserProfile;
    private Alert testAlert;

    @BeforeEach
    void setUp() {
        testUser = new UserSummary(1L, "Ana", "García", "ana@test.com", null, 1L, null);
        adminUser = new UserSummary(2L, "Admin", "User", "admin@test.com", null, 2L, null);
        testUserProfile = 10L;
        otherUserProfile = 20L;

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

        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(testUserProfile));
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
        verify(identityApi, times(1)).findProfileId(1L);
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void testCreateAlert_ProfileNotFound_ThrowsException() {
        // Arrange
        Alert newAlert = new Alert();
        newAlert.setDeviceId(1L);

        when(identityApi.findProfileId(999L)).thenReturn(Optional.empty());

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
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(testUserProfile));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        // Act - Ahora pasamos userId (1L) no userProfileId (10L)
        Alert result = alertService.updateAlert(1L, updateData, 1L);

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

        Long otherProfile = 999L;

        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(identityApi.findProfileId(999L)).thenReturn(Optional.of(otherProfile));

        // Act & Assert - userId diferente (999L) no es dueño de la alerta
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
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(testUserProfile));
        doNothing().when(alertRepository).deleteById(1L);

        // Act - Ahora pasamos userId (1L) no userProfileId (10L)
        alertService.deleteAlert(1L, 1L);

        // Assert
        verify(alertRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAlert_UnauthorizedOwner_ThrowsAccessDenied() {
        // Arrange
        Long otherProfile = 999L;

        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(identityApi.findProfileId(999L)).thenReturn(Optional.of(otherProfile));

        // Act & Assert - userId diferente (999L) no es dueño de la alerta
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
            alertService.deleteAlert(1L, 999L)
        );
        assertEquals("No puedes eliminar alertas de otro usuario", exception.getMessage());
        verify(alertRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAlertsByUserProfile_OwnerAccessWithUserId() {
        // Los clientes envían users.id (1), que no coincide con user_profile.id (10)
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(testUserProfile));
        when(identityApi.findUser(1L)).thenReturn(Optional.of(testUser));
        when(alertRepository.findByUserProfileId(10L)).thenReturn(List.of(testAlert));

        List<Alert> results = alertService.getAlertsByUserProfile(1L, 1L);

        assertEquals(1, results.size());
        verify(alertRepository).findByUserProfileId(10L);
    }

    @Test
    void testGetAlertsByUserProfile_OwnerAccess() {
        // Arrange
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(testUserProfile));
        when(identityApi.findUser(1L)).thenReturn(Optional.of(testUser));
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
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(testUserProfile));
        when(identityApi.findUser(1L)).thenReturn(Optional.of(testUser));

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
        Long adminProfile = 50L;

        when(identityApi.findProfileId(2L)).thenReturn(Optional.of(adminProfile));
        when(identityApi.findUser(2L)).thenReturn(Optional.of(adminUser));
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
        when(identityApi.findProfileId(1L)).thenReturn(Optional.of(testUserProfile));

        // Act - Pasar userId (1L) que tiene userProfileId 10L
        Optional<Alert> result = alertService.getAlertById(1L, 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAlert.getId(), result.get().getId());
    }

    @Test
    void testGetAlertById_WrongOwner_ReturnsEmpty() {
        // Arrange
        Long otherProfile = 999L;

        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(identityApi.findProfileId(999L)).thenReturn(Optional.of(otherProfile));

        // Act - Pasar userId diferente (999L) que tiene userProfileId 999L
        Optional<Alert> result = alertService.getAlertById(1L, 999L);

        // Assert
        assertFalse(result.isPresent());
    }
}

package com.alertamujer.admin.application.service;

import com.alertamujer.admin.infrastructure.controller.dto.AdminUserDto;
import com.alertamujer.admin.infrastructure.controller.dto.UpdateUserRequest;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/** Las reglas de las cuentas se prueban en UserAccountAdminServiceTest (módulo identity). */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private IdentityAdminApi identityAdminApi;

    @Mock
    private AlertsApi alertsApi;

    @InjectMocks
    private AdminService adminService;

    private UserAccountView cuenta(Long userId, Long profileId) {
        return new UserAccountView(userId, profileId, "Ana", "Gómez", "ana@test.co", null, "user", "active", null, null);
    }

    @Test
    void getAllUsers_agregaElNumeroDeAlertasDeCadaUsuaria() {
        when(identityAdminApi.listUserAccounts()).thenReturn(List.of(cuenta(1L, 10L), cuenta(2L, null)));
        when(alertsApi.alertCountByProfile()).thenReturn(Map.of(10L, 3L));

        List<AdminUserDto> usuarias = adminService.getAllUsers();

        assertEquals(3L, usuarias.get(0).alertCount());
        assertEquals(0L, usuarias.get(1).alertCount()); // sin perfil de app
    }

    @Test
    void updateUser_delegaEnIdentityConElResponsable() {
        when(identityAdminApi.updateUserAccount(1L, new UserAccountChanges("inactive", null, null, null, null), 9L))
                .thenReturn(cuenta(1L, 10L));
        when(alertsApi.alertCountByProfile()).thenReturn(Map.of());

        AdminUserDto dto = adminService.updateUser(1L, new UpdateUserRequest("inactive", null, null, null, null), 9L);

        assertEquals(1L, dto.id());
        assertEquals(0L, dto.alertCount());
    }
}

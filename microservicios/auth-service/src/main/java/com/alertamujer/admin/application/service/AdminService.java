package com.alertamujer.admin.application.service;

import com.alertamujer.admin.infrastructure.controller.dto.AdminUserDto;
import com.alertamujer.admin.infrastructure.controller.dto.UpdateUserRequest;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.identity.api.IdentityAdminApi;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Gestión de usuarias del panel. Compone dos módulos por sus APIs públicas: las cuentas
 * (identity, que valida y guarda los cambios) y el número de alertas de cada una (alerts).
 */
@Service
public class AdminService {

    private final IdentityAdminApi identityAdminApi;
    private final AlertsApi alertsApi;

    public AdminService(IdentityAdminApi identityAdminApi, AlertsApi alertsApi) {
        this.identityAdminApi = identityAdminApi;
        this.alertsApi = alertsApi;
    }

    public List<AdminUserDto> getAllUsers() {
        Map<Long, Long> alertasPorPerfil = alertsApi.alertCountByProfile();
        return identityAdminApi.listUserAccounts().stream()
                .map(v -> dto(v, alertasPorPerfil))
                .toList();
    }

    /**
     * Actualiza la cuenta; identity valida el estado, impide bloquearse a sí misma y el correo repetido.
     */
    public AdminUserDto updateUser(Long userId, UpdateUserRequest request, Long currentUserId) {
        UserAccountView v = identityAdminApi.updateUserAccount(userId, new UserAccountChanges(
                request.accountStatus(), request.firstName(), request.lastName(), request.telephone(),
                request.email()), currentUserId);
        return dto(v, alertsApi.alertCountByProfile());
    }

    private static AdminUserDto dto(UserAccountView v, Map<Long, Long> alertasPorPerfil) {
        long alertas = v.profileId() == null ? 0L : alertasPorPerfil.getOrDefault(v.profileId(), 0L);
        return new AdminUserDto(v.userId(), v.profileId(), v.firstName(), v.lastName(), v.email(), v.telephone(),
                v.roleName(), v.accountStatus(), v.createdAt(), v.lastAccess(), alertas);
    }
}

package com.alertamujer.admin.adapter.in.web.dto;

import com.alertamujer.admin.application.port.in.ManageUsersUseCase.UserWithAlerts;
import com.alertamujer.identity.api.UserAccountView;

import java.time.LocalDateTime;

/** Usuaria en la lista del panel, con su número de alertas. */
public record AdminUserDto(Long id, Long userProfileId, String firstName, String lastName, String email, String telephone,
                           String role, String accountStatus, LocalDateTime createdAt, LocalDateTime lastAccess,
                           Long alertCount) {

    public static AdminUserDto of(UserWithAlerts u) {
        UserAccountView v = u.account();
        return new AdminUserDto(v.userId(), v.profileId(), v.firstName(), v.lastName(), v.email(), v.telephone(),
                v.roleName(), v.accountStatus(), v.createdAt(), v.lastAccess(), u.alerts());
    }
}

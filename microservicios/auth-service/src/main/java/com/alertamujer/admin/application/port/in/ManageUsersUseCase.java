package com.alertamujer.admin.application.port.in;

import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;

import java.util.List;

/** Gestión de usuarias del panel: las cuentas (identity) con su número de alertas (Alert Service). */
public interface ManageUsersUseCase {

    List<UserWithAlerts> listAll();

    UserWithAlerts update(Long userId, UserAccountChanges changes, Long actor);

    record UserWithAlerts(UserAccountView account, long alerts) {}
}

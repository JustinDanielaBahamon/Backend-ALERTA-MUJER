package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.ManageUsersUseCase;
import com.alertamujer.admin.application.port.out.AlertsPort;
import com.alertamujer.admin.application.port.out.AccountsPort;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Joins the accounts (identity) with the alert count of each one (Alert Service). */
@Service
public class UserManagementService implements ManageUsersUseCase {

    private final AccountsPort accounts;
    private final AlertsPort alerts;

    public UserManagementService(AccountsPort accounts, AlertsPort alerts) {
        this.accounts = accounts;
        this.alerts = alerts;
    }

    @Override
    public List<UserWithAlerts> listAll() {
        Map<Long, Long> byProfile = alerts.countByProfile();
        return accounts.listAll().stream().map(c -> withAlerts(c, byProfile)).toList();
    }

    @Override
    public UserWithAlerts update(Long userId, UserAccountChanges changes, Long actor) {
        return withAlerts(accounts.update(userId, changes, actor), alerts.countByProfile());
    }

    private static UserWithAlerts withAlerts(UserAccountView c, Map<Long, Long> byProfile) {
        return new UserWithAlerts(c, c.profileId() == null ? 0L : byProfile.getOrDefault(c.profileId(), 0L));
    }
}

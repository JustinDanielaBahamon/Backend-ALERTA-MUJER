package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.out.AlertsPort;
import com.alertamujer.admin.application.port.out.AccountsPort;
import com.alertamujer.admin.application.port.out.InfrastructurePort;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;
import com.alertamujer.identity.api.UserAccountView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** User management and system status: join identity and Alert Service through their ports. */
class UserManagementServiceTest {

    private final AccountsPort accounts = mock(AccountsPort.class);
    private final AlertsPort alerts = mock(AlertsPort.class);

    private static UserAccountView account(Long userId, Long profile) {
        return new UserAccountView(userId, profile, "Ana", "Gómez", "ana@correo.com", null, "user", "active", null, null);
    }

    @Test
    void eachUserCarriesHerAlertCount() {
        when(accounts.listAll()).thenReturn(List.of(account(1L, 10L), account(2L, null), account(3L, 30L)));
        when(alerts.countByProfile()).thenReturn(Map.of(10L, 4L));

        var r = new UserManagementService(accounts, alerts).listAll();

        assertEquals(List.of(4L, 0L, 0L), r.stream().map(u -> u.alerts()).toList());
    }

    @Test
    void withoutDatabaseActiveAlertsAreNotQueried() {
        InfrastructurePort infra = mock(InfrastructurePort.class);
        when(infra.databaseResponds()).thenReturn(false);
        when(alerts.lastExpiration()).thenReturn(new ExpirationStatus(null, 0));

        var status = new SystemStatusService(infra, alerts).status();

        assertFalse(status.database());
        assertEquals(-1, status.activeAlerts());
        verify(alerts, never()).active();
    }
}

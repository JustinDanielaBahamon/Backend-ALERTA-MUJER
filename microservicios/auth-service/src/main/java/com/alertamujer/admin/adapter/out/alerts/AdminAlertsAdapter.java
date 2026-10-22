package com.alertamujer.admin.adapter.out.alerts;

import com.alertamujer.admin.application.port.out.AlertsPort;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Output adapter to Alert Service: in Auth Service, AlertsApi is an HTTP client (/internal/rpc). */
@Component
class AdminAlertsAdapter implements AlertsPort {

    private final AlertsApi alertsApi;

    AdminAlertsAdapter(AlertsApi alertsApi) {
        this.alertsApi = alertsApi;
    }

    @Override
    public Map<Long, Long> countByProfile() {
        return alertsApi.alertCountByProfile();
    }

    @Override
    public List<AdminAlertView> all() {
        return alertsApi.adminAlertViews();
    }

    @Override
    public long active() {
        return alertsApi.countActiveAlerts();
    }

    @Override
    public ExpirationStatus lastExpiration() {
        return alertsApi.expirationStatus();
    }
}

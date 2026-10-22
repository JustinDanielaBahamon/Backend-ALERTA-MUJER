package com.alertamujer.admin.adapter.out.alerts;

import com.alertamujer.admin.application.port.out.AlertsPort;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Adaptador de salida hacia Alert Service: en Auth Service, AlertsApi es un cliente HTTP (/internal/rpc). */
@Component
class AdminAlertasAdapter implements AlertsPort {

    private final AlertsApi alertsApi;

    AdminAlertasAdapter(AlertsApi alertsApi) {
        this.alertsApi = alertsApi;
    }

    @Override
    public Map<Long, Long> conteoPorPerfil() {
        return alertsApi.alertCountByProfile();
    }

    @Override
    public List<AdminAlertView> todas() {
        return alertsApi.adminAlertViews();
    }

    @Override
    public long activas() {
        return alertsApi.countActiveAlerts();
    }

    @Override
    public ExpirationStatus ultimaExpiracion() {
        return alertsApi.expirationStatus();
    }
}

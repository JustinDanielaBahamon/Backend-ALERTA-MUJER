package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.SystemStatusUseCase;
import com.alertamujer.admin.application.port.out.AlertsPort;
import com.alertamujer.admin.application.port.out.InfrastructurePort;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Estado del servidor para el panel: base de datos, push, alertas activas y última expiración. */
@Service
public class SystemStatusService implements SystemStatusUseCase {

    private final InfrastructurePort infrastructure;
    private final AlertsPort alerts;

    public SystemStatusService(InfrastructurePort infrastructure, AlertsPort alerts) {
        this.infrastructure = infrastructure;
        this.alerts = alerts;
    }

    @Override
    public SystemStatus status() {
        long start = System.nanoTime();
        boolean database = infrastructure.databaseResponds();
        long latencyMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
        long active = database ? alerts.active() : -1;
        ExpirationStatus expiration = alerts.lastExpiration();
        return new SystemStatus(database, latencyMs, infrastructure.pushConfigured(), active, expiration.lastCheck(),
                expiration.lastExpiredCount(), infrastructure.startup(), infrastructure.versionJava());
    }
}

package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.EstadoSistemaUseCase;
import com.alertamujer.admin.application.port.out.AlertsPort;
import com.alertamujer.admin.application.port.out.InfraestructuraPort;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Estado del servidor para el panel: base de datos, push, alertas activas y última expiración. */
@Service
public class EstadoSistemaService implements EstadoSistemaUseCase {

    private final InfraestructuraPort infraestructura;
    private final AlertsPort alerts;

    public EstadoSistemaService(InfraestructuraPort infraestructura, AlertsPort alerts) {
        this.infraestructura = infraestructura;
        this.alerts = alerts;
    }

    @Override
    public SystemStatus estado() {
        long inicio = System.nanoTime();
        boolean baseDatos = infraestructura.baseDeDatosResponde();
        long latenciaMs = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
        long activas = baseDatos ? alerts.activas() : -1;
        ExpirationStatus expiracion = alerts.ultimaExpiracion();
        return new SystemStatus(baseDatos, latenciaMs, infraestructura.pushConfigurado(), activas, expiracion.lastCheck(),
                expiracion.lastExpiredCount(), infraestructura.arranque(), infraestructura.versionJava());
    }
}

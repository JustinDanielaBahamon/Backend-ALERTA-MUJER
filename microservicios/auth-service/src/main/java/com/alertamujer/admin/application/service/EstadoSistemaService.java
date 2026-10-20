package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.EstadoSistemaUseCase;
import com.alertamujer.admin.application.port.out.AlertasPort;
import com.alertamujer.admin.application.port.out.InfraestructuraPort;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Estado del servidor para el panel: base de datos, push, alertas activas y última expiración. */
@Service
public class EstadoSistemaService implements EstadoSistemaUseCase {

    private final InfraestructuraPort infraestructura;
    private final AlertasPort alertas;

    public EstadoSistemaService(InfraestructuraPort infraestructura, AlertasPort alertas) {
        this.infraestructura = infraestructura;
        this.alertas = alertas;
    }

    @Override
    public SystemStatus estado() {
        long inicio = System.nanoTime();
        boolean baseDatos = infraestructura.baseDeDatosResponde();
        long latenciaMs = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
        long activas = baseDatos ? alertas.activas() : -1;
        ExpirationStatus expiracion = alertas.ultimaExpiracion();
        return new SystemStatus(baseDatos, latenciaMs, infraestructura.pushConfigurado(), activas, expiracion.lastCheck(),
                expiracion.lastExpiredCount(), infraestructura.arranque(), infraestructura.versionJava());
    }
}

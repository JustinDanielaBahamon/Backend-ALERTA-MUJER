package com.alertamujer.microservice;

import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.shared.rpc.ContratosExpuestos;
import com.alertamujer.shared.rpc.EventosRemotos;
import com.alertamujer.shared.rpc.RpcClientFactory;
import com.alertamujer.zones.api.ZonesApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cómo se conecta Auth Service con los otros microservicios.
 * - Atiende IdentityApi, DevicesApi y SettingsApi para Alert y Evidence.
 * - Usa AlertsApi y ZonesApi de Alert Service (el panel de administración).
 */
@Configuration
public class AuthMicroservicioConfig {

    @Bean
    ContratosExpuestos contratosExpuestos() {
        return new ContratosExpuestos(List.of(IdentityApi.class, DevicesApi.class, SettingsApi.class));
    }

    @Bean
    AlertsApi alertsApi(RpcClientFactory clientes, @Value("${services.alert.url}") String url) {
        return clientes.create(AlertsApi.class, url);
    }

    @Bean
    ZonesApi zonesApi(RpcClientFactory clientes, @Value("${services.alert.url}") String url) {
        return clientes.create(ZonesApi.class, url);
    }

    @Bean
    EventosRemotos eventosHaciaAlertas(@Value("${services.alert.url}") String url,
                                       @Value("${internal.api-key:}") String key, ObjectMapper json) {
        return new EventosRemotos(url, key, json);
    }

    @Bean
    EventosRemotos eventosHaciaEvidencias(@Value("${services.evidence.url}") String url,
                                          @Value("${internal.api-key:}") String key, ObjectMapper json) {
        return new EventosRemotos(url, key, json);
    }
}

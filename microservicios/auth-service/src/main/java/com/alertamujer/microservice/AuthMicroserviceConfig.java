package com.alertamujer.microservice;

import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.shared.rpc.ExposedContracts;
import com.alertamujer.shared.rpc.RemoteEvents;
import com.alertamujer.shared.rpc.RpcClientFactory;
import com.alertamujer.zones.api.ZonesApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * How Auth Service connects to the other microservices.
 * - Serves IdentityApi, DevicesApi and SettingsApi for Alert and Evidence.
 * - Uses AlertsApi and ZonesApi from Alert Service (the admin panel).
 */
@Configuration
public class AuthMicroserviceConfig {

    @Bean
    ExposedContracts exposedContracts() {
        return new ExposedContracts(List.of(IdentityApi.class, DevicesApi.class, SettingsApi.class));
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
    RemoteEvents eventsToAlerts(@Value("${services.alert.url}") String url,
                                       @Value("${internal.api-key:}") String key, ObjectMapper json) {
        return new RemoteEvents(url, key, json);
    }

    @Bean
    RemoteEvents eventsToEvidence(@Value("${services.evidence.url}") String url,
                                          @Value("${internal.api-key:}") String key, ObjectMapper json) {
        return new RemoteEvents(url, key, json);
    }
}

package com.alertamujer.microservice;

import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.AccountDeletedEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.shared.rpc.ExposedContracts;
import com.alertamujer.shared.rpc.ReceivedEventsController.AcceptedEvents;
import com.alertamujer.shared.rpc.RpcClientFactory;
import com.alertamujer.zones.api.ZonesApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cómo se conecta Alert Service con los otros microservicios.
 * - Atiende AlertsApi y ZonesApi (para Auth y Evidence).
 * - Usa IdentityApi, DevicesApi y SettingsApi de Auth Service y EvidenceApi de Evidence Service.
 * - Recibe de Auth los eventos de recuperación de contraseña y de celular nuevo (envía el correo/push).
 */
@Configuration
public class AlertMicroservicioConfig {

    @Bean
    ExposedContracts contratosExpuestos() {
        return new ExposedContracts(List.of(AlertsApi.class, ZonesApi.class));
    }

    @Bean
    AcceptedEvents acceptedEvents() {
        return new AcceptedEvents(List.of(PasswordRecoveryRequestedEvent.class, NewPhoneSignedInEvent.class,
                AccountDeletedEvent.class));
    }

    @Bean
    IdentityApi identityApi(RpcClientFactory clientes, @Value("${services.auth.url}") String url) {
        return clientes.create(IdentityApi.class, url);
    }

    @Bean
    DevicesApi devicesApi(RpcClientFactory clientes, @Value("${services.auth.url}") String url) {
        return clientes.create(DevicesApi.class, url);
    }

    @Bean
    SettingsApi settingsApi(RpcClientFactory clientes, @Value("${services.auth.url}") String url) {
        return clientes.create(SettingsApi.class, url);
    }
}

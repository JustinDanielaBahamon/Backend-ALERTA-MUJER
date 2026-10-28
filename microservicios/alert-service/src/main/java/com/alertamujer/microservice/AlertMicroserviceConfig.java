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
 * How Alert Service connects to the other microservices.
 * - Serves AlertsApi and ZonesApi (for Auth and Evidence).
 * - Uses IdentityApi, DevicesApi and SettingsApi from Auth Service and EvidenceApi from Evidence Service.
 * - Receives from Auth the password recovery and new phone events (sends the email/push).
 */
@Configuration
public class AlertMicroserviceConfig {

    @Bean
    ExposedContracts exposedContracts() {
        return new ExposedContracts(List.of(AlertsApi.class, ZonesApi.class));
    }

    @Bean
    AcceptedEvents acceptedEvents() {
        return new AcceptedEvents(List.of(PasswordRecoveryRequestedEvent.class, NewPhoneSignedInEvent.class,
                AccountDeletedEvent.class));
    }

    @Bean
    IdentityApi identityApi(RpcClientFactory clients, @Value("${services.auth.url}") String url) {
        return clients.create(IdentityApi.class, url);
    }

    @Bean
    DevicesApi devicesApi(RpcClientFactory clients, @Value("${services.auth.url}") String url) {
        return clients.create(DevicesApi.class, url);
    }

    @Bean
    SettingsApi settingsApi(RpcClientFactory clients, @Value("${services.auth.url}") String url) {
        return clients.create(SettingsApi.class, url);
    }
}

package com.alertamujer.microservice;

import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.identity.api.AccountDeletedEvent;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.rpc.ReceivedEventsController.AcceptedEvents;
import com.alertamujer.shared.rpc.RpcClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * How Evidence Service connects to the other microservices: it uses AlertsApi from Alert Service
 * (does the alert exist and belong to the user? is it kept?) and IdentityApi from Auth Service. The
 * AlertsAdapter and IdentityAdapter adapters do not change: they receive these HTTP clients.
 */
@Configuration
public class EvidenceMicroserviceConfig {

    /** Auth Service reports when an account is deleted, so its recordings are removed. */
    @Bean
    AcceptedEvents acceptedEvents() {
        return new AcceptedEvents(List.of(AccountDeletedEvent.class));
    }

    @Bean
    AlertsApi alertsApi(RpcClientFactory clientes, @Value("${services.alert.url}") String url) {
        return clientes.create(AlertsApi.class, url);
    }

    @Bean
    IdentityApi identityApi(RpcClientFactory clientes, @Value("${services.auth.url}") String url) {
        return clientes.create(IdentityApi.class, url);
    }
}

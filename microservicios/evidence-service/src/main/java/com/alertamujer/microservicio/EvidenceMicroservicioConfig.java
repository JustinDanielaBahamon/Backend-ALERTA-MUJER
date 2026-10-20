package com.alertamujer.microservicio;

import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.identity.api.AccountDeletedEvent;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.rpc.EventosRecibidosController.EventosAceptados;
import com.alertamujer.shared.rpc.RpcClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cómo se conecta Evidence Service con los otros microservicios: usa AlertsApi de Alert Service
 * (¿la alerta existe y es de la usuaria?, ¿la conserva?) e IdentityApi de Auth Service. Los
 * adaptadores AlertasAdapter e IdentidadAdapter no cambian: reciben estos clientes HTTP.
 */
@Configuration
public class EvidenceMicroservicioConfig {

    /** Auth Service avisa cuando se elimina una cuenta, para borrar sus grabaciones. */
    @Bean
    EventosAceptados eventosAceptados() {
        return new EventosAceptados(List.of(AccountDeletedEvent.class));
    }

    @Bean
    AlertsApi alertsApi(RpcClientFactory clientes, @Value("${services.alert.url}") String url) {
        return clientes.crear(AlertsApi.class, url);
    }

    @Bean
    IdentityApi identityApi(RpcClientFactory clientes, @Value("${services.auth.url}") String url) {
        return clientes.crear(IdentityApi.class, url);
    }
}

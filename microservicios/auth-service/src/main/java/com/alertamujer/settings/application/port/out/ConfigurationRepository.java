package com.alertamujer.settings.application.port.out;

import com.alertamujer.settings.domain.model.SystemConfiguration;

import java.util.Optional;

/** Puerto de salida: la fila única de configuración. */
public interface ConfigurationRepository {

    Optional<SystemConfiguration> current();

    SystemConfiguration save(SystemConfiguration config);
}

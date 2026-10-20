package com.alertamujer.settings.application.port.out;

import com.alertamujer.settings.domain.model.SystemConfiguration;

import java.util.Optional;

/** Puerto de salida: la fila única de configuración. */
public interface ConfiguracionRepositorio {

    Optional<SystemConfiguration> actual();

    SystemConfiguration guardar(SystemConfiguration configuracion);
}

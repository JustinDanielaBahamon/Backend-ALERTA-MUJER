package com.alertamujer.settings.application.port.out;

import com.alertamujer.settings.domain.model.SystemConfiguration;

import java.util.Optional;

/** Output port: the single configuration row. */
public interface ConfigurationRepository {

    Optional<SystemConfiguration> current();

    SystemConfiguration save(SystemConfiguration config);
}

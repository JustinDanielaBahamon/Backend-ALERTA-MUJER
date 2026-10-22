package com.alertamujer.settings.application.port.in;

import com.alertamujer.settings.domain.model.SystemConfiguration;

/** Lo que leen las apps y los otros módulos y microservicios. */
public interface QuerySettingsUseCase {

    /** Para las apps de cualquier usuaria: número de emergencia, soporte, duración y versión mínima. */
    SystemConfiguration publicPaths();

    /** 0 si no hay configuración (no expira ninguna alerta). */
    int maxAlertDurationMinutes();

    /** "123" si no hay configuración. */
    String emergencyNumber();

    /** 5 si no hay configuración. */
    int maxContacts();
}

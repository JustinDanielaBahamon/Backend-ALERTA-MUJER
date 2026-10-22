package com.alertamujer.notifications.adapter.out.settings;

import com.alertamujer.notifications.application.port.out.AjustesPort;
import com.alertamujer.settings.api.SettingsApi;
import org.springframework.stereotype.Component;

/** Adaptador de salida hacia settings (cliente HTTP a Auth Service). */
@Component
class NotificacionesAjustesAdapter implements AjustesPort {

    private final SettingsApi settingsApi;

    NotificacionesAjustesAdapter(SettingsApi settingsApi) {
        this.settingsApi = settingsApi;
    }

    @Override
    public String emergencyNumber() {
        return settingsApi.emergencyNumber();
    }
}

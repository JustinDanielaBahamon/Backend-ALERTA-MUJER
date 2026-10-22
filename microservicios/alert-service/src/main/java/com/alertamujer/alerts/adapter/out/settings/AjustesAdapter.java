package com.alertamujer.alerts.adapter.out.settings;

import com.alertamujer.alerts.application.port.out.AjustesPort;
import com.alertamujer.settings.api.SettingsApi;
import org.springframework.stereotype.Component;

/** Adaptador de salida hacia settings (cliente HTTP a Auth Service). */
@Component
class AjustesAdapter implements AjustesPort {

    private final SettingsApi settingsApi;

    AjustesAdapter(SettingsApi settingsApi) {
        this.settingsApi = settingsApi;
    }

    @Override
    public int maxAlertDurationMinutes() {
        return settingsApi.maxAlertDurationMinutes();
    }

    @Override
    public int maxContacts() {
        return settingsApi.maxContacts();
    }
}

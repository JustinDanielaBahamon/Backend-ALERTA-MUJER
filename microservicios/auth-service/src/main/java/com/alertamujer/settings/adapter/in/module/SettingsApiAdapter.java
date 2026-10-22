package com.alertamujer.settings.adapter.in.module;

import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.settings.application.port.in.QuerySettingsUseCase;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: el contrato SettingsApi para los otros módulos y para Alert Service (por /internal/rpc). */
@Component
class SettingsApiAdapter implements SettingsApi {

    private final QuerySettingsUseCase queries;

    SettingsApiAdapter(QuerySettingsUseCase queries) {
        this.queries = queries;
    }

    @Override
    public int maxAlertDurationMinutes() {
        return queries.maxAlertDurationMinutes();
    }

    @Override
    public String emergencyNumber() {
        return queries.emergencyNumber();
    }

    @Override
    public int maxContacts() {
        return queries.maxContacts();
    }
}

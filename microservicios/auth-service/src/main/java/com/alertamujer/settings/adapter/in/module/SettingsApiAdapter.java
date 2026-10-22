package com.alertamujer.settings.adapter.in.module;

import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.settings.application.port.in.QuerySettingsUseCase;
import org.springframework.stereotype.Component;

/** Input adapter: the SettingsApi contract for the other modules and for Alert Service (through /internal/rpc). */
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

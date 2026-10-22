package com.alertamujer.settings.adapter.in.module;

import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.settings.application.port.in.ConsultarAjustesUseCase;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: el contrato SettingsApi para los otros módulos y para Alert Service (por /internal/rpc). */
@Component
class SettingsApiAdapter implements SettingsApi {

    private final ConsultarAjustesUseCase consultas;

    SettingsApiAdapter(ConsultarAjustesUseCase consultas) {
        this.consultas = consultas;
    }

    @Override
    public int duracionMaximaAlertaMinutos() {
        return consultas.duracionMaximaAlertaMinutos();
    }

    @Override
    public String numeroEmergencia() {
        return consultas.numeroEmergencia();
    }

    @Override
    public int maximoContactos() {
        return consultas.maximoContactos();
    }
}

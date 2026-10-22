package com.alertamujer.devices.adapter.in.module;

import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.devices.application.port.in.ConsultarDispositivosUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de entrada: el contrato DevicesApi para los otros módulos y para Alert Service (por /internal/rpc). */
@Component
class DevicesApiAdapter implements DevicesApi {

    private final ConsultarDispositivosUseCase consultas;

    DevicesApiAdapter(ConsultarDispositivosUseCase consultas) {
        this.consultas = consultas;
    }

    @Override
    public List<String> pushTokensOfProfile(Long userProfileId) {
        return consultas.tokensDelPerfil(userProfileId);
    }

    @Override
    public List<String> pushTokensOfUser(Long userId) {
        return consultas.tokensDeUsuaria(userId);
    }

    @Override
    public List<String> phonePushTokensOfUser(Long userId) {
        return consultas.tokensDeCelulares(userId);
    }

    @Override
    public boolean isMostRecentPhone(Long userId, Long deviceId) {
        return consultas.esElCelularMasReciente(userId, deviceId);
    }

    @Override
    public long countDevices() {
        return consultas.contar();
    }
}

package com.alertamujer.notifications.adapter.out.devices;

import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.notifications.application.port.out.DispositivosPort;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida hacia devices (cliente HTTP a Auth Service). */
@Component
class DispositivosAdapter implements DispositivosPort {

    private final DevicesApi devicesApi;

    DispositivosAdapter(DevicesApi devicesApi) {
        this.devicesApi = devicesApi;
    }

    @Override
    public List<String> tokensDe(Long userId) {
        return devicesApi.pushTokensOfUser(userId);
    }
}

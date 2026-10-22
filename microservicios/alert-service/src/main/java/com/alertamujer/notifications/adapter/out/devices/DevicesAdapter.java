package com.alertamujer.notifications.adapter.out.devices;

import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.notifications.application.port.out.DevicesPort;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida hacia devices (cliente HTTP a Auth Service). */
@Component
class DevicesAdapter implements DevicesPort {

    private final DevicesApi devicesApi;

    DevicesAdapter(DevicesApi devicesApi) {
        this.devicesApi = devicesApi;
    }

    @Override
    public List<String> tokensOf(Long userId) {
        return devicesApi.pushTokensOfUser(userId);
    }
}

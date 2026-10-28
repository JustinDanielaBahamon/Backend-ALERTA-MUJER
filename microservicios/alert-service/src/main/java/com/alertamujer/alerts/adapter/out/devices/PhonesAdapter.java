package com.alertamujer.alerts.adapter.out.devices;

import com.alertamujer.alerts.application.port.out.PhonesPort;
import com.alertamujer.devices.api.DevicesApi;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida hacia devices (cliente HTTP a Auth Service). */
@Component
class PhonesAdapter implements PhonesPort {

    private final DevicesApi devicesApi;

    PhonesAdapter(DevicesApi devicesApi) {
        this.devicesApi = devicesApi;
    }

    @Override
    public boolean isTheNewest(Long userId, Long deviceId) {
        return devicesApi.isMostRecentPhone(userId, deviceId);
    }

    @Override
    public List<String> phoneTokens(Long userId) {
        return devicesApi.phonePushTokensOfUser(userId);
    }
}

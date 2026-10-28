package com.alertamujer.devices.adapter.in.module;

import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.devices.application.port.in.QueryDevicesUseCase;
import org.springframework.stereotype.Component;

import java.util.List;

/** Input adapter: the DevicesApi contract for the other modules and for Alert Service (through /internal/rpc). */
@Component
class DevicesApiAdapter implements DevicesApi {

    private final QueryDevicesUseCase queries;

    DevicesApiAdapter(QueryDevicesUseCase queries) {
        this.queries = queries;
    }

    @Override
    public List<String> pushTokensOfProfile(Long userProfileId) {
        return queries.profileTokens(userProfileId);
    }

    @Override
    public List<String> pushTokensOfUser(Long userId) {
        return queries.userTokens(userId);
    }

    @Override
    public List<String> phonePushTokensOfUser(Long userId) {
        return queries.phoneTokens(userId);
    }

    @Override
    public boolean isMostRecentPhone(Long userId, Long deviceId) {
        return queries.isTheNewestPhone(userId, deviceId);
    }

    @Override
    public long countDevices() {
        return queries.count();
    }
}

package com.alertamujer.devices.adapter.in.web.dto;

import com.alertamujer.devices.domain.model.DeviceData;

import java.time.LocalDateTime;

/** Device data sent by the app or the web. The account comes from the token: an id or accountId in the body is ignored. */
public record DeviceRequest(String deviceUuid, String brand, String model, String deviceName, String osName,
                            String osVersion, String appVersion, String gpsStatus, String status,
                            LocalDateTime lastAccess, String fcmToken, String permissions) {

    public DeviceData toData() {
        return new DeviceData(deviceUuid, brand, model, deviceName, osName, osVersion, appVersion, gpsStatus,
                status, lastAccess, fcmToken, permissions);
    }
}

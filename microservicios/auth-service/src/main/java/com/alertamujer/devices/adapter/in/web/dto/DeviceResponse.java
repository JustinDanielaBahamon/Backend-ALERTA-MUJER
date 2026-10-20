package com.alertamujer.devices.adapter.in.web.dto;

import com.alertamujer.devices.domain.model.Device;

import java.time.LocalDateTime;

/** Dispositivo como lo recibe su dueña (mismos campos y orden de siempre). */
public record DeviceResponse(Long id, Long accountId, String deviceUuid, String brand, String model, String deviceName,
                             String osName, String osVersion, String appVersion, String gpsStatus, String status,
                             LocalDateTime lastAccess, LocalDateTime createdAt, String fcmToken, String permissions,
                             LocalDateTime permissionsUpdatedAt) {

    public static DeviceResponse of(Device d) {
        return new DeviceResponse(d.getId(), d.getAccountId(), d.getDeviceUuid(), d.getBrand(), d.getModel(),
                d.getDeviceName(), d.getOsName(), d.getOsVersion(), d.getAppVersion(), d.getGpsStatus(), d.getStatus(),
                d.getLastAccess(), d.getCreatedAt(), d.getFcmToken(), d.getPermissions(), d.getPermissionsUpdatedAt());
    }
}

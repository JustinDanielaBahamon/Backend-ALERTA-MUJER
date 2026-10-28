package com.alertamujer.devices.domain.model;

import java.time.LocalDateTime;

/** Data of a device sent by the app or the web. When reporting use, a null field is left as it was. */
public record DeviceData(String deviceUuid, String brand, String model, String deviceName, String osName,
                               String osVersion, String appVersion, String gpsStatus, String status,
                               LocalDateTime lastAccess, String fcmToken, String permissions) {
}

package com.alertamujer.devices.domain.model;

import java.time.LocalDateTime;

/** Datos de un dispositivo que envía la app o la web. Al informar el uso, un campo null se deja como estaba. */
public record DeviceData(String deviceUuid, String brand, String model, String deviceName, String osName,
                               String osVersion, String appVersion, String gpsStatus, String status,
                               LocalDateTime lastAccess, String fcmToken, String permissions) {
}

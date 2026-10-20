package com.alertamujer.devices.adapter.in.web.dto;

import com.alertamujer.devices.domain.model.DatosDispositivo;

import java.time.LocalDateTime;

/** Datos del dispositivo que envían la app o la web. La cuenta sale del token: un id o accountId en el body se ignora. */
public record DeviceRequest(String deviceUuid, String brand, String model, String deviceName, String osName,
                            String osVersion, String appVersion, String gpsStatus, String status,
                            LocalDateTime lastAccess, String fcmToken, String permissions) {

    public DatosDispositivo aDatos() {
        return new DatosDispositivo(deviceUuid, brand, model, deviceName, osName, osVersion, appVersion, gpsStatus,
                status, lastAccess, fcmToken, permissions);
    }
}

package com.alertamujer.devices.adapter.in.web.dto;

import com.alertamujer.devices.application.port.in.AdministrarDispositivosUseCase.DispositivoConDuena;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.identity.api.UserSummary;

import java.time.LocalDateTime;

/** Dispositivo con su dueña para el panel. No expone el token push, solo si existe. */
public record AdminDeviceView(Long id, Long accountId, Long userId, String ownerName, String ownerEmail,
                              String ownerPhone, String deviceUuid, String brand, String model,
                              String osName, String osVersion, String appVersion, String gpsStatus,
                              String status, LocalDateTime lastAccess, LocalDateTime createdAt,
                              boolean pushTokenRegistered, String deviceName,
                              /** JSON de permisos que informa la app ({"ubicacion":true,...}). */
                              String permissions, LocalDateTime permissionsUpdatedAt) {

    public static AdminDeviceView of(DispositivoConDuena dc) {
        Device d = dc.dispositivo();
        UserSummary u = dc.duena();
        return new AdminDeviceView(d.getId(), d.getAccountId(), u != null ? u.id() : null,
                u == null ? null : u.fullName(), u != null ? u.email() : null, u != null ? u.telephone() : null,
                d.getDeviceUuid(), d.getBrand(), d.getModel(), d.getOsName(), d.getOsVersion(),
                d.getAppVersion(), d.getGpsStatus(), d.getStatus(), d.getLastAccess(), d.getCreatedAt(),
                d.tieneTokenPush(), d.getDeviceName(), d.getPermissions(), d.getPermissionsUpdatedAt());
    }
}

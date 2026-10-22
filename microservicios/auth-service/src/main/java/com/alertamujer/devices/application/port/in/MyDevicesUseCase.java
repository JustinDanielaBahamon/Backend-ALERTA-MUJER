package com.alertamujer.devices.application.port.in;

import com.alertamujer.devices.domain.model.DeviceData;
import com.alertamujer.devices.domain.model.Device;

import java.util.List;

/** La persona en sesión y sus dispositivos. Solo puede tocar los de su cuenta. */
public interface MyDevicesUseCase {

    List<Device> mine(Long userId);

    Device one(Long id, Long userId);

    /** Registra el dispositivo en la cuenta; si es otro celular, avisa a los demás celulares. */
    Device registrar(DeviceData data, Long userId);

    Device replace(Long id, DeviceData data, Long userId);

    /** La app informa los datos actuales del celular, sus permisos y la última conexión. */
    Device recordUse(Long id, DeviceData data, Long userId);

    void changePushToken(Long id, String token, Long userId);

    void delete(Long id, Long userId);
}

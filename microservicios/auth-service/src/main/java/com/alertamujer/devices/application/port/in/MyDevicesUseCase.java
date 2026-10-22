package com.alertamujer.devices.application.port.in;

import com.alertamujer.devices.domain.model.DeviceData;
import com.alertamujer.devices.domain.model.Device;

import java.util.List;

/** The signed-in person and her devices. She can only touch the ones of her account. */
public interface MyDevicesUseCase {

    List<Device> mine(Long userId);

    Device one(Long id, Long userId);

    /** Registers the device in the account; if it is another phone, warns the other phones. */
    Device registrar(DeviceData data, Long userId);

    Device replace(Long id, DeviceData data, Long userId);

    /** The app reports the phone's current data, its permissions and the last connection. */
    Device recordUse(Long id, DeviceData data, Long userId);

    void changePushToken(Long id, String token, Long userId);

    void delete(Long id, Long userId);
}

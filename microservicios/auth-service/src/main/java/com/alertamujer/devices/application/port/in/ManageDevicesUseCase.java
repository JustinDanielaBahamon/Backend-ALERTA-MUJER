package com.alertamujer.devices.application.port.in;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.identity.api.UserSummary;

import java.util.List;

/** La administración ve, cambia de estado y borra cualquier dispositivo, con los datos de su dueña. */
public interface ManageDevicesUseCase {

    /** Del usado más recientemente al más antiguo. */
    List<DeviceWithOwner> listAll();

    DeviceWithOwner get(Long id);

    /** active, inactive, blocked o lost. */
    DeviceWithOwner changeStatus(Long id, String status);

    void delete(Long id);

    record DeviceWithOwner(Device device, UserSummary owner) {}
}

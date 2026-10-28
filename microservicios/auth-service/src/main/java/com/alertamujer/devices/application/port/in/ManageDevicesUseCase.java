package com.alertamujer.devices.application.port.in;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.identity.api.UserSummary;

import java.util.List;

/** The administration sees, changes the status of and deletes any device, with its owner's data. */
public interface ManageDevicesUseCase {

    /** From most recently used to oldest. */
    List<DeviceWithOwner> listAll();

    DeviceWithOwner get(Long id);

    /** active, inactive, blocked o lost. */
    DeviceWithOwner changeStatus(Long id, String status);

    void delete(Long id);

    record DeviceWithOwner(Device device, UserSummary owner) {}
}

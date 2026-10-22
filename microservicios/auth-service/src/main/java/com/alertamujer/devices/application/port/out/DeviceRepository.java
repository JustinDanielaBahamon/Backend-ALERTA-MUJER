package com.alertamujer.devices.application.port.out;

import com.alertamujer.devices.domain.model.Device;

import java.util.List;
import java.util.Optional;

/** Output port: the devices. */
public interface DeviceRepository {

    Device save(Device device);

    Optional<Device> find(Long id);

    List<Device> all();

    List<Device> ofAccount(Long accountId);

    void delete(Long id);

    long count();
}

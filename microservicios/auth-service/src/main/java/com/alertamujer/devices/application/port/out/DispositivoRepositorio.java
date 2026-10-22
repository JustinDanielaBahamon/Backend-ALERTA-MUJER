package com.alertamujer.devices.application.port.out;

import com.alertamujer.devices.domain.model.Device;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los dispositivos. */
public interface DispositivoRepositorio {

    Device save(Device device);

    Optional<Device> find(Long id);

    List<Device> todos();

    List<Device> deCuenta(Long accountId);

    void delete(Long id);

    long contar();
}

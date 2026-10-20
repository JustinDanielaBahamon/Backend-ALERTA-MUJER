package com.alertamujer.devices.application.port.out;

import com.alertamujer.devices.domain.model.Device;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los dispositivos. */
public interface DispositivoRepositorio {

    Device guardar(Device dispositivo);

    Optional<Device> buscar(Long id);

    List<Device> todos();

    List<Device> deCuenta(Long accountId);

    void eliminar(Long id);

    long contar();
}

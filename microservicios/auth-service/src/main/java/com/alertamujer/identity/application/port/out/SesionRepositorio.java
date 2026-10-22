package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.DeviceSession;

import java.util.List;

/** Puerto de salida: las sesiones de los dispositivos. */
public interface SesionRepositorio {

    List<DeviceSession> activasDeCuenta(Long accountId);

    DeviceSession save(DeviceSession sesion);
}

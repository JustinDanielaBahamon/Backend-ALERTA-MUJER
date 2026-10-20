package com.alertamujer.devices.application.port.in;

import com.alertamujer.devices.domain.model.DatosDispositivo;
import com.alertamujer.devices.domain.model.Device;

import java.util.List;

/** La persona en sesión y sus dispositivos. Solo puede tocar los de su cuenta. */
public interface MisDispositivosUseCase {

    List<Device> mios(Long userId);

    Device uno(Long id, Long userId);

    /** Registra el dispositivo en la cuenta; si es otro celular, avisa a los demás celulares. */
    Device registrar(DatosDispositivo datos, Long userId);

    Device reemplazar(Long id, DatosDispositivo datos, Long userId);

    /** La app informa los datos actuales del celular, sus permisos y la última conexión. */
    Device registrarUso(Long id, DatosDispositivo datos, Long userId);

    void cambiarTokenPush(Long id, String token, Long userId);

    void eliminar(Long id, Long userId);
}

package com.alertamujer.devices.application.port.out;

import java.util.List;

/** Puerto de salida: avisos al resto del sistema. */
public interface EventosDispositivosPort {

    /** Se entró a la cuenta desde un celular nuevo; Alert Service avisa a los otros celulares. */
    void nuevoCelular(String nombreCelular, List<String> tokensOtrosCelulares);
}

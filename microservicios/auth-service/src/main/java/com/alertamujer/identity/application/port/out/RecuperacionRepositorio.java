package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.RecoveryRequest;

import java.util.List;

/** Puerto de salida: las solicitudes de recuperación de contraseña. */
public interface RecuperacionRepositorio {

    RecoveryRequest guardar(RecoveryRequest solicitud);

    void guardarTodas(List<RecoveryRequest> solicitudes);

    List<RecoveryRequest> pendientesDeCuenta(Long accountId);

    boolean existeHash(String hash);
}

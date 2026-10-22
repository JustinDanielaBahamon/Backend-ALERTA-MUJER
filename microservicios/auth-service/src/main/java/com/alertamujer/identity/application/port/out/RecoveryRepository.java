package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.RecoveryRequest;

import java.util.List;

/** Puerto de salida: las solicitudes de recuperación de contraseña. */
public interface RecoveryRepository {

    RecoveryRequest save(RecoveryRequest deletionRequest);

    void saveAll(List<RecoveryRequest> solicitudes);

    List<RecoveryRequest> pendingForAccount(Long accountId);

    boolean hashExists(String hash);
}

package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.AccountDeletionRequest;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las solicitudes de eliminación de cuentas. */
public interface SolicitudEliminacionRepositorio {

    /** De la más reciente a la más antigua. */
    List<AccountDeletionRequest> todas();

    Optional<AccountDeletionRequest> buscar(Long id);

    boolean hayPendiente(Long userId);

    AccountDeletionRequest guardar(AccountDeletionRequest solicitud);
}

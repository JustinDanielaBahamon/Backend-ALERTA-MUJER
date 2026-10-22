package com.alertamujer.admin.application.port.in;

import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;

import java.util.List;

/** Gestión de usuarias del panel: las cuentas (identity) con su número de alertas (Alert Service). */
public interface GestionUsuariasUseCase {

    List<UsuariaConAlertas> listAll();

    UsuariaConAlertas actualizar(Long userId, UserAccountChanges cambios, Long quienActua);

    record UsuariaConAlertas(UserAccountView cuenta, long alerts) {}
}

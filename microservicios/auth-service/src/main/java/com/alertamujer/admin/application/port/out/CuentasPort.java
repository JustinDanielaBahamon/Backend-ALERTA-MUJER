package com.alertamujer.admin.application.port.out;

import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;

import java.util.List;

/** Puerto de salida hacia identity: la administración de cuentas (identity guarda los datos y sus reglas). */
public interface CuentasPort {

    List<UserAccountView> listar();

    /** identity valida el estado, impide que una administradora se bloquee a sí misma y el correo repetido. */
    UserAccountView actualizar(Long userId, UserAccountChanges cambios, Long quienActua);

    /** Borra los datos personales, cierra las sesiones y deja la cuenta inactiva. */
    void anonimizar(Long userId);
}

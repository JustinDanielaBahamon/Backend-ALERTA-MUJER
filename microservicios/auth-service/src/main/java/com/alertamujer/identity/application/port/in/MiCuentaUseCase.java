package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.CambiosUsuaria;
import com.alertamujer.identity.domain.model.User;

/** La persona en sesión consulta y cambia sus datos. */
public interface MiCuentaUseCase {

    UsuariaConFoto mia(Long userId);

    UsuariaConFoto actualizar(Long userId, CambiosUsuaria cambios);

    /** Exige la contraseña actual para evitar cambios desde una sesión ajena. */
    void cambiarContrasena(Long userId, String actual, String nueva);

    record UsuariaConFoto(User usuaria, String foto) {}
}

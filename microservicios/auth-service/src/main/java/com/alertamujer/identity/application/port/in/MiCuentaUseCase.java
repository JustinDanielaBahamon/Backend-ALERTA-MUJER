package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.CambiosUsuaria;
import com.alertamujer.identity.domain.model.User;

/** La persona en sesión consulta y cambia sus datos. */
public interface MiCuentaUseCase {

    UsuariaConFoto mia(Long userId);

    UsuariaConFoto actualizar(Long userId, CambiosUsuaria cambios);

    /** Exige la contraseña actual para evitar cambios desde una sesión ajena. */
    void cambiarContrasena(Long userId, String current, String nueva);

    /**
     * La propia usuaria elimina su cuenta escribiendo la palabra de confirmación. Las cuentas de
     * administración no: necesitan la aprobación de otra administradora desde el panel.
     */
    void eliminarMiCuenta(Long userId, String confirmacion);

    /** Palabra que hay que escribir para confirmar la eliminación. */
    String CONFIRMACION_ELIMINAR = "ELIMINAR";

    record UsuariaConFoto(User usuaria, String photo) {}
}

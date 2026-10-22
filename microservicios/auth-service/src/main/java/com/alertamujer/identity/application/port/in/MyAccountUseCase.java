package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.UserChanges;
import com.alertamujer.identity.domain.model.User;

/** La persona en sesión consulta y cambia sus datos. */
public interface MyAccountUseCase {

    UserWithPhoto mine(Long userId);

    UserWithPhoto update(Long userId, UserChanges changes);

    /** Exige la contraseña actual para evitar cambios desde una sesión ajena. */
    void changePassword(Long userId, String current, String newOne);

    /**
     * La propia usuaria elimina su cuenta escribiendo la palabra de confirmación. Las cuentas de
     * administración no: necesitan la aprobación de otra administradora desde el panel.
     */
    void deleteMyAccount(Long userId, String confirmation);

    /** Palabra que hay que escribir para confirmar la eliminación. */
    String DELETE_CONFIRMATION = "ELIMINAR";

    record UserWithPhoto(User user, String photo) {}
}

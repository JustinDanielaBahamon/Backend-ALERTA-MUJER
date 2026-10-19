package com.alertamujer.identity.api;

import java.util.List;

/** Operaciones de administración de cuentas que el módulo identity ofrece al panel. */
public interface IdentityAdminApi {

    List<UserAccountView> listUserAccounts();

    /**
     * Aplica los cambios a la persona y su cuenta. Valida el estado de la cuenta, impide que una
     * administradora se bloquee a sí misma (actingUserId) y que el correo quede repetido.
     */
    UserAccountView updateUserAccount(Long userId, UserAccountChanges changes, Long actingUserId);

    /**
     * "Elimina" la cuenta sin romper el historial: borra los datos personales (nombre, correo,
     * teléfono, documento, foto), cierra sus sesiones y la deja inactiva sin poder entrar.
     */
    void anonymizeAccount(Long userId);
}

package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.AccountChanges;
import com.alertamujer.identity.domain.model.User;

import java.util.List;

/** Administración de cuentas desde el panel. */
public interface ManageAccountsUseCase {

    List<UserAccount> listAll();

    /** Valida el estado, impide que una administradora se bloquee a sí misma y que el correo se repita. */
    UserAccount update(Long userId, AccountChanges changes, Long actor);

    /** Borra los datos personales, cierra sus sesiones y la deja inactiva sin poder entrar. */
    void anonymize(Long userId);

    record UserAccount(User user, Account account, Long profileId) {}
}

package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.Account;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las cuentas (credenciales y estado). */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> find(Long id);

    Optional<Account> ofUser(Long userId);

    List<Account> all();
}

package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.Account;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las cuentas (credenciales y estado). */
public interface CuentaRepositorio {

    Account guardar(Account cuenta);

    Optional<Account> buscar(Long id);

    Optional<Account> deUsuaria(Long userId);

    List<Account> todas();
}

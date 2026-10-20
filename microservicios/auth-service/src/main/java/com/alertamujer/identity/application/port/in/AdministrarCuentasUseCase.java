package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.CambiosCuenta;
import com.alertamujer.identity.domain.model.User;

import java.util.List;

/** Administración de cuentas desde el panel. */
public interface AdministrarCuentasUseCase {

    List<CuentaDeUsuaria> listar();

    /** Valida el estado, impide que una administradora se bloquee a sí misma y que el correo se repita. */
    CuentaDeUsuaria actualizar(Long userId, CambiosCuenta cambios, Long quienActua);

    /** Borra los datos personales, cierra sus sesiones y la deja inactiva sin poder entrar. */
    void anonimizar(Long userId);

    record CuentaDeUsuaria(User usuaria, Account cuenta, Long perfilId) {}
}

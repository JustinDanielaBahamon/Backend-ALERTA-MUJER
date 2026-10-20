package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.GestionUsuariasUseCase;
import com.alertamujer.admin.application.port.out.AlertasPort;
import com.alertamujer.admin.application.port.out.CuentasPort;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserAccountView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Compone las cuentas (identity) con el número de alertas de cada una (Alert Service). */
@Service
public class GestionUsuariasService implements GestionUsuariasUseCase {

    private final CuentasPort cuentas;
    private final AlertasPort alertas;

    public GestionUsuariasService(CuentasPort cuentas, AlertasPort alertas) {
        this.cuentas = cuentas;
        this.alertas = alertas;
    }

    @Override
    public List<UsuariaConAlertas> listar() {
        Map<Long, Long> porPerfil = alertas.conteoPorPerfil();
        return cuentas.listar().stream().map(c -> conAlertas(c, porPerfil)).toList();
    }

    @Override
    public UsuariaConAlertas actualizar(Long userId, UserAccountChanges cambios, Long quienActua) {
        return conAlertas(cuentas.actualizar(userId, cambios, quienActua), alertas.conteoPorPerfil());
    }

    private static UsuariaConAlertas conAlertas(UserAccountView c, Map<Long, Long> porPerfil) {
        return new UsuariaConAlertas(c, c.profileId() == null ? 0L : porPerfil.getOrDefault(c.profileId(), 0L));
    }
}

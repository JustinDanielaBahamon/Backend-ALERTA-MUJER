package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.GestionUsuariasUseCase;
import com.alertamujer.admin.application.port.out.AlertsPort;
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
    private final AlertsPort alerts;

    public GestionUsuariasService(CuentasPort cuentas, AlertsPort alerts) {
        this.cuentas = cuentas;
        this.alerts = alerts;
    }

    @Override
    public List<UsuariaConAlertas> listAll() {
        Map<Long, Long> byProfile = alerts.conteoPorPerfil();
        return cuentas.listAll().stream().map(c -> conAlertas(c, byProfile)).toList();
    }

    @Override
    public UsuariaConAlertas actualizar(Long userId, UserAccountChanges cambios, Long quienActua) {
        return conAlertas(cuentas.actualizar(userId, cambios, quienActua), alerts.conteoPorPerfil());
    }

    private static UsuariaConAlertas conAlertas(UserAccountView c, Map<Long, Long> byProfile) {
        return new UsuariaConAlertas(c, c.profileId() == null ? 0L : byProfile.getOrDefault(c.profileId(), 0L));
    }
}

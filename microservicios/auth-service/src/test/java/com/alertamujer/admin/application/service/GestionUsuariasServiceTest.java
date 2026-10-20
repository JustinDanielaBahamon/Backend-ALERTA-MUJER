package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.out.AlertasPort;
import com.alertamujer.admin.application.port.out.CuentasPort;
import com.alertamujer.admin.application.port.out.InfraestructuraPort;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;
import com.alertamujer.identity.api.UserAccountView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Gestión de usuarias y estado del sistema: componen identity y Alert Service por sus puertos. */
class GestionUsuariasServiceTest {

    private final CuentasPort cuentas = mock(CuentasPort.class);
    private final AlertasPort alertas = mock(AlertasPort.class);

    private static UserAccountView cuenta(Long userId, Long perfil) {
        return new UserAccountView(userId, perfil, "Ana", "Gómez", "ana@correo.com", null, "user", "active", null, null);
    }

    @Test
    void cadaUsuariaLlevaSuNumeroDeAlertas() {
        when(cuentas.listar()).thenReturn(List.of(cuenta(1L, 10L), cuenta(2L, null), cuenta(3L, 30L)));
        when(alertas.conteoPorPerfil()).thenReturn(Map.of(10L, 4L));

        var r = new GestionUsuariasService(cuentas, alertas).listar();

        assertEquals(List.of(4L, 0L, 0L), r.stream().map(u -> u.alertas()).toList());
    }

    @Test
    void sinBaseDeDatosNoSeConsultanLasAlertasActivas() {
        InfraestructuraPort infra = mock(InfraestructuraPort.class);
        when(infra.baseDeDatosResponde()).thenReturn(false);
        when(alertas.ultimaExpiracion()).thenReturn(new ExpirationStatus(null, 0));

        var estado = new EstadoSistemaService(infra, alertas).estado();

        assertFalse(estado.database());
        assertEquals(-1, estado.activeAlerts());
        verify(alertas, never()).activas();
    }
}

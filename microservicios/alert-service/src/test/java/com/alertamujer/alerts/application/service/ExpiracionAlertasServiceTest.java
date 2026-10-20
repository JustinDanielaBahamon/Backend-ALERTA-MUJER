package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.AjustesPort;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.shared.web.AuditSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiracionAlertasServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Mock private AlertaRepositorio repositorio;
    @Mock private AjustesPort ajustes;
    @Mock private EventosAlertaPort eventos;

    @InjectMocks private ExpiracionAlertasService service;

    private static Alert alerta(LocalDateTime inicio) {
        Alert a = new Alert();
        a.setStartedAt(inicio);
        return a;
    }

    @Test
    void expiraSoloLasQuePasanLaDuracionMaximaYAvisa() {
        Alert vieja = alerta(AHORA.minusMinutes(31));
        Alert reciente = alerta(AHORA.minusMinutes(10));
        when(ajustes.duracionMaximaAlertaMinutos()).thenReturn(30);
        when(repositorio.activas()).thenReturn(List.of(vieja, reciente));

        assertEquals(1, service.revisar(AHORA));

        assertEquals("expired", vieja.getStatus());
        assertEquals(AHORA, vieja.getEndedAt());
        assertEquals("active", reciente.getStatus());
        verify(repositorio).guardarTodas(List.of(vieja));
        verify(eventos).alertaTerminada(vieja, AHORA);
        verifyNoMoreInteractions(eventos);
        assertEquals(AHORA, service.ultimaRevision());
        assertEquals(1, service.expiradasUltimaRevision());
    }

    @Test
    void sinConfiguracionNoExpiraNada() {
        when(ajustes.duracionMaximaAlertaMinutos()).thenReturn(0);

        assertEquals(0, service.revisar(AHORA));
        verifyNoInteractions(repositorio, eventos);
    }

    @Test
    void laRevisionRegistraElOrigenDeLaTareaParaLaAuditoria() {
        when(ajustes.duracionMaximaAlertaMinutos()).thenReturn(30);
        when(repositorio.activas()).thenReturn(List.of());
        String[] origen = new String[1];
        when(repositorio.guardarTodas(List.of())).thenAnswer(inv -> {
            origen[0] = AuditSource.actual();
            return List.of();
        });

        service.revisar(AHORA);

        assertEquals("Tarea automática · Expiración de alertas", origen[0]);
        assertNull(AuditSource.actual());
    }
}

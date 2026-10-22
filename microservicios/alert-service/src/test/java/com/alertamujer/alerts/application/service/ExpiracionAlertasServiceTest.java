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

    @Mock private AlertaRepositorio repository;
    @Mock private AjustesPort ajustes;
    @Mock private EventosAlertaPort events;

    @InjectMocks private ExpiracionAlertasService service;

    private static Alert alert(LocalDateTime inicio) {
        Alert a = new Alert();
        a.setStartedAt(inicio);
        return a;
    }

    @Test
    void expiraSoloLasQuePasanLaDuracionMaximaYAvisa() {
        Alert vieja = alert(AHORA.minusMinutes(31));
        Alert reciente = alert(AHORA.minusMinutes(10));
        when(ajustes.duracionMaximaAlertaMinutos()).thenReturn(30);
        when(repository.activas()).thenReturn(List.of(vieja, reciente));

        assertEquals(1, service.revisar(AHORA));

        assertEquals("expired", vieja.getStatus());
        assertEquals(AHORA, vieja.getEndedAt());
        assertEquals("active", reciente.getStatus());
        verify(repository).guardarTodas(List.of(vieja));
        verify(events).alertaTerminada(vieja, AHORA);
        verifyNoMoreInteractions(events);
        assertEquals(AHORA, service.ultimaRevision());
        assertEquals(1, service.expiradasUltimaRevision());
    }

    @Test
    void sinConfiguracionNoExpiraNada() {
        when(ajustes.duracionMaximaAlertaMinutos()).thenReturn(0);

        assertEquals(0, service.revisar(AHORA));
        verifyNoInteractions(repository, events);
    }

    @Test
    void laRevisionRegistraElOrigenDeLaTareaParaLaAuditoria() {
        when(ajustes.duracionMaximaAlertaMinutos()).thenReturn(30);
        when(repository.activas()).thenReturn(List.of());
        String[] origen = new String[1];
        when(repository.guardarTodas(List.of())).thenAnswer(inv -> {
            origen[0] = AuditSource.actual();
            return List.of();
        });

        service.revisar(AHORA);

        assertEquals("Tarea automática · Expiración de alertas", origen[0]);
        assertNull(AuditSource.actual());
    }
}

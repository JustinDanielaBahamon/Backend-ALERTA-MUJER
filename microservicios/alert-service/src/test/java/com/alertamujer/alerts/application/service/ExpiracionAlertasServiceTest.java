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

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Mock private AlertaRepositorio repository;
    @Mock private AjustesPort ajustes;
    @Mock private EventosAlertaPort events;

    @InjectMocks private ExpiracionAlertasService service;

    private static Alert alert(LocalDateTime start) {
        Alert a = new Alert();
        a.setStartedAt(start);
        return a;
    }

    @Test
    void expiraSoloLasQuePasanLaDuracionMaximaYAvisa() {
        Alert vieja = alert(NOW.minusMinutes(31));
        Alert recent = alert(NOW.minusMinutes(10));
        when(ajustes.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of(vieja, recent));

        assertEquals(1, service.revisar(NOW));

        assertEquals("expired", vieja.getStatus());
        assertEquals(NOW, vieja.getEndedAt());
        assertEquals("active", recent.getStatus());
        verify(repository).saveAll(List.of(vieja));
        verify(events).alertaTerminada(vieja, NOW);
        verifyNoMoreInteractions(events);
        assertEquals(NOW, service.ultimaRevision());
        assertEquals(1, service.expiradasUltimaRevision());
    }

    @Test
    void sinConfiguracionNoExpiraNada() {
        when(ajustes.maxAlertDurationMinutes()).thenReturn(0);

        assertEquals(0, service.revisar(NOW));
        verifyNoInteractions(repository, events);
    }

    @Test
    void laRevisionRegistraElOrigenDeLaTareaParaLaAuditoria() {
        when(ajustes.maxAlertDurationMinutes()).thenReturn(30);
        when(repository.active()).thenReturn(List.of());
        String[] source = new String[1];
        when(repository.saveAll(List.of())).thenAnswer(inv -> {
            source[0] = AuditSource.current();
            return List.of();
        });

        service.revisar(NOW);

        assertEquals("Tarea automática · Expiración de alertas", source[0]);
        assertNull(AuditSource.current());
    }
}

package com.alertamujer.settings.application.service;

import com.alertamujer.settings.application.port.out.ConfiguracionRepositorio;
import com.alertamujer.settings.domain.model.CambiosAjustes;
import com.alertamujer.settings.domain.model.SystemConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AjustesServiceTest {

    private final ConfiguracionRepositorio repositorio = mock(ConfiguracionRepositorio.class);
    private final AjustesService service = new AjustesService(repositorio);

    @Test
    void sinConfiguracionUsaValoresSegurosParaLosOtrosModulos() {
        when(repositorio.actual()).thenReturn(Optional.empty());

        assertEquals(0, service.duracionMaximaAlertaMinutos());
        assertEquals("123", service.numeroEmergencia());
        assertEquals(5, service.maximoContactos());
        assertThrows(IllegalStateException.class, service::obtener);
    }

    @Test
    void actualizarGuardaYDevuelveLaHoraDelCambio() {
        SystemConfiguration c = new SystemConfiguration();
        c.setSosMaxDurationMinutes(120);
        when(repositorio.actual()).thenReturn(Optional.of(c));
        when(repositorio.guardar(any())).thenAnswer(inv -> {
            SystemConfiguration guardada = inv.getArgument(0);
            guardada.setUpdatedAt(null); // la base no devuelve la hora del trigger
            return guardada;
        });

        SystemConfiguration r = service.actualizar(new CambiosAjustes(90, null, null, null, null), 7L);

        assertEquals(90, r.getSosMaxDurationMinutes());
        assertNotNull(r.getUpdatedAt());
        verify(repositorio).guardar(c);
    }
}

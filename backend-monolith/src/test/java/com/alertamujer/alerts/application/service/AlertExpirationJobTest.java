package com.alertamujer.alerts.application.service;

import com.alertamujer.admin.application.service.AdminSettingsService;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertExpirationJobTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AdminSettingsService settingsService;

    @InjectMocks
    private AlertExpirationJob job;

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 15, 12, 0);

    private Alert alerta(LocalDateTime inicio) {
        Alert a = new Alert();
        a.setStartedAt(inicio);
        return a;
    }

    @Test
    void expiraSoloLasQuePasanLaDuracionMaxima() {
        Alert vieja = alerta(AHORA.minusMinutes(31));
        Alert reciente = alerta(AHORA.minusMinutes(10));
        when(settingsService.duracionMaximaAlertaMinutos()).thenReturn(30);
        when(alertRepository.findByStatus("active")).thenReturn(List.of(vieja, reciente));

        assertEquals(1, job.expirarVencidas(AHORA));

        assertEquals("expired", vieja.getStatus());
        assertEquals(AHORA, vieja.getEndedAt());
        assertEquals("active", reciente.getStatus());
        assertNull(reciente.getEndedAt());
        verify(alertRepository).saveAll(List.of(vieja));
    }

    @Test
    void sinConfiguracionNoExpiraNada() {
        when(settingsService.duracionMaximaAlertaMinutos()).thenReturn(0);

        assertEquals(0, job.expirarVencidas(AHORA));
    }
}

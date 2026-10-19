package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.infrastructure.repository.LocationLogRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocationRetentionJobTest {

    @Test
    void borraLoDelDiaADiaALos7DiasYLasAlertasALos30() {
        LocationLogRepository repository = mock(LocationLogRepository.class);
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 19, 3, 30);
        when(repository.borrarSinAlertaAntesDe(ahora.minusDays(7))).thenReturn(120);
        when(repository.borrarDeAlertasAntesDe(ahora.minusDays(30))).thenReturn(15);

        int borradas = new LocationRetentionJob(repository, 7, 30).limpiar(ahora);

        assertEquals(135, borradas);
        verify(repository).borrarSinAlertaAntesDe(ahora.minusDays(7));
        verify(repository).borrarDeAlertasAntesDe(ahora.minusDays(30));
    }
}

package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.out.LocationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LocationCleanupServiceTest {

    @Test
    void deletesDailyDataAfter7DaysAndAlertsAfter30() {
        LocationRepository repository = mock(LocationRepository.class);
        LocalDateTime now = LocalDateTime.of(2026, 10, 19, 3, 30);
        when(repository.deleteWithoutAlertBefore(now.minusDays(7))).thenReturn(120);
        when(repository.deleteFromAlertsBefore(now.minusDays(30))).thenReturn(15);

        assertEquals(135, new LocationCleanupService(repository, 7, 30).clear(now));

        verify(repository).deleteWithoutAlertBefore(now.minusDays(7));
        verify(repository).deleteFromAlertsBefore(now.minusDays(30));
    }
}

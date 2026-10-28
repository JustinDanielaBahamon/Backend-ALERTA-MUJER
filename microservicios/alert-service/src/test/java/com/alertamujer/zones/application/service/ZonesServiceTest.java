package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.out.ZoneRepository;
import com.alertamujer.zones.domain.model.ZoneData;
import com.alertamujer.zones.domain.model.Zone;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZonesServiceTest {

    private final ZoneRepository repository = mock(ZoneRepository.class);
    private final ZonesService service = new ZonesService(repository);

    @Test
    void countsOnlyActiveZones() {
        Zone active = new Zone();
        Zone inactive = new Zone();
        inactive.setIsActive(false);
        when(repository.all()).thenReturn(List.of(active, inactive));

        assertEquals(1, service.countActive());
    }

    @Test
    void editingAMissingZoneReturns404() {
        when(repository.find(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.replace(9L,
                new ZoneData(null, "x", "risk", null, null, null, "x", null, null, null, null)));
    }
}

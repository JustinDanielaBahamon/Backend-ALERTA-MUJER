package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.out.ZonaRepositorio;
import com.alertamujer.zones.domain.model.DatosZona;
import com.alertamujer.zones.domain.model.Zone;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZonasServiceTest {

    private final ZonaRepositorio repository = mock(ZonaRepositorio.class);
    private final ZonasService service = new ZonasService(repository);

    @Test
    void cuentaSoloLasZonasActivas() {
        Zone activa = new Zone();
        Zone inactiva = new Zone();
        inactiva.setIsActive(false);
        when(repository.all()).thenReturn(List.of(activa, inactiva));

        assertEquals(1, service.contarActivas());
    }

    @Test
    void editarUnaZonaInexistenteResponde404() {
        when(repository.find(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.replace(9L,
                new DatosZona(null, "x", "risk", null, null, null, "x", null, null, null, null)));
    }
}

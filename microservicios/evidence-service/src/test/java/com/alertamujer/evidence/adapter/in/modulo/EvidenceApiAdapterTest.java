package com.alertamujer.evidence.adapter.in.modulo;

import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.domain.model.Evidence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvidenceApiAdapterTest {

    private final EvidenciaRepositorio repositorio = mock(EvidenciaRepositorio.class);
    private final EvidenceApiAdapter api = new EvidenceApiAdapter(repositorio, mock(AlmacenamientoEvidencias.class));

    private Evidence evidencia(Long id, Long alertId, String key) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(alertId);
        e.setMediaType("audio");
        e.setStorageKey(key);
        return e;
    }

    @Test
    void soloListaEvidenciasConArchivo() {
        when(repositorio.deLaAlerta(5L)).thenReturn(List.of(evidencia(1L, 5L, "clave"), evidencia(2L, 5L, null)));

        var lista = api.evidenciasDeAlerta(5L);

        assertEquals(1, lista.size());
        assertEquals(1L, lista.get(0).id());
    }

    @Test
    void noEntregaElArchivoDeOtraAlerta() {
        when(repositorio.buscar(1L)).thenReturn(Optional.of(evidencia(1L, 7L, "clave")));

        assertTrue(api.archivo(5L, 1L).isEmpty());
    }
}

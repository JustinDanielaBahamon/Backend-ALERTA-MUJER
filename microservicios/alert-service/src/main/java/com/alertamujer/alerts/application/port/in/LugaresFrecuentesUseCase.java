package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.DatosLugar;
import com.alertamujer.alerts.domain.model.FrequentLocation;

import java.util.List;

/** Lugares frecuentes de la usuaria. */
public interface LugaresFrecuentesUseCase {

    /** Todos (solo administración). */
    List<FrequentLocation> todos();

    List<FrequentLocation> activosDe(Long userId);

    FrequentLocation uno(Long id, Long userId);

    FrequentLocation crear(DatosLugar datos, Long userId);

    FrequentLocation actualizar(Long id, DatosLugar datos, Long userId);

    void eliminar(Long id, Long userId);
}

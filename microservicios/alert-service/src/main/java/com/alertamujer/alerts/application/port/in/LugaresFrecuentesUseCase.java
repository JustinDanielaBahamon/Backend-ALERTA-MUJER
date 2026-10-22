package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.DatosLugar;
import com.alertamujer.alerts.domain.model.FrequentLocation;

import java.util.List;

/** Lugares frecuentes de la usuaria. */
public interface LugaresFrecuentesUseCase {

    List<FrequentLocation> activosDe(Long userId);

    FrequentLocation uno(Long id, Long userId);

    FrequentLocation create(DatosLugar data, Long userId);

    FrequentLocation actualizar(Long id, DatosLugar data, Long userId);

    void delete(Long id, Long userId);
}

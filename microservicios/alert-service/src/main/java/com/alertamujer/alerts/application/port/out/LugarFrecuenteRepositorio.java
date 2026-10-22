package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.FrequentLocation;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los lugares frecuentes. */
public interface LugarFrecuenteRepositorio {

    List<FrequentLocation> all();

    List<FrequentLocation> activosDeLaUsuaria(Long profile);

    Optional<FrequentLocation> find(Long id);

    FrequentLocation save(FrequentLocation lugar);

    void delete(Long id);
}

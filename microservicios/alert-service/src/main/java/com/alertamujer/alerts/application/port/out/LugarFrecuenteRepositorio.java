package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.FrequentLocation;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los lugares frecuentes. */
public interface LugarFrecuenteRepositorio {

    List<FrequentLocation> todos();

    List<FrequentLocation> activosDeLaUsuaria(Long perfil);

    Optional<FrequentLocation> buscar(Long id);

    FrequentLocation guardar(FrequentLocation lugar);

    void eliminar(Long id);
}

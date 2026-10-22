package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.SavedRoute;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los recorridos guardados. */
public interface RecorridoRepositorio {

    /** Los más nuevos primero. */
    List<SavedRoute> ofUser(Long profile);

    Optional<SavedRoute> find(Long id);

    long count(Long profile);

    long contarEstrella(Long profile);

    SavedRoute save(SavedRoute recorrido);

    void delete(Long id);
}

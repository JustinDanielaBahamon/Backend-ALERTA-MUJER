package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.SavedRoute;

import java.util.List;

/** Recorridos guardados de la usuaria (compartidos entre web y móvil). */
public interface RecorridosUseCase {

    List<SavedRoute> mios(Long userId);

    SavedRoute create(NuevoRecorrido nuevo, Long userId);

    /** Renombrar y/o marcar o desmarcar estrella (null = sin cambio). */
    SavedRoute actualizar(Long id, String name, Boolean estrella, Long userId);

    void delete(Long id, Long userId);
}

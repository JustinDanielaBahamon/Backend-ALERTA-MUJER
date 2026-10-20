package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.SavedRoute;

import java.util.List;

/** Recorridos guardados de la usuaria (compartidos entre web y móvil). */
public interface RecorridosUseCase {

    List<SavedRoute> mios(Long userId);

    SavedRoute crear(NuevoRecorrido nuevo, Long userId);

    /** Renombrar y/o marcar o desmarcar estrella (null = sin cambio). */
    SavedRoute actualizar(Long id, String nombre, Boolean estrella, Long userId);

    void eliminar(Long id, Long userId);
}

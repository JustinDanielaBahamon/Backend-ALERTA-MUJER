package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.SavedRoute;

import java.util.List;

/** Recorridos guardados de la usuaria (compartidos entre web y móvil). */
public interface RoutesUseCase {

    List<SavedRoute> mine(Long userId);

    SavedRoute create(NewRoute created, Long userId);

    /** Renombrar y/o marcar o desmarcar estrella (null = sin cambio). */
    SavedRoute update(Long id, String name, Boolean starred, Long userId);

    void delete(Long id, Long userId);
}

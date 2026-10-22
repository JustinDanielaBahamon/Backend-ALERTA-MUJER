package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.UserReport;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los casos de moderación. */
public interface ReporteUsuariaRepositorio {

    List<UserReport> todos();

    List<UserReport> conEstado(String status);

    Optional<UserReport> find(Long id);

    /** Casos sobre una usuaria (users.id). */
    List<UserReport> deUsuaria(Long userId);

    UserReport save(UserReport reporte);

    void delete(Long id);
}

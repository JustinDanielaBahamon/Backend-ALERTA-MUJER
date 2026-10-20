package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.Alert;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: dónde se guardan las alertas. */
public interface AlertaRepositorio {

    Alert guardar(Alert alerta);

    List<Alert> guardarTodas(List<Alert> alertas);

    Optional<Alert> buscar(Long id);

    /** Las que no existen se omiten. */
    List<Alert> buscarVarias(Collection<Long> ids);

    List<Alert> todas();

    List<Alert> deLaUsuaria(Long perfil);

    List<Alert> activas();

    /** La alerta activa más reciente de la usuaria. */
    Optional<Alert> enCurso(Long perfil);

    /** Alertas de la usuaria marcadas para conservar sus evidencias. */
    long conservadas(Long perfil);

    void eliminar(Long id);
}

package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.Alert;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: dónde se guardan las alertas. */
public interface AlertaRepositorio {

    Alert save(Alert alert);

    List<Alert> saveAll(List<Alert> alerts);

    Optional<Alert> find(Long id);

    /** Las que no existen se omiten. */
    List<Alert> findMany(Collection<Long> ids);

    List<Alert> all();

    List<Alert> ofUser(Long profile);

    List<Alert> active();

    /** La alerta activa más reciente de la usuaria. */
    Optional<Alert> enCurso(Long profile);

    /** Alertas de la usuaria marcadas para conservar sus evidencias. */
    long kept(Long profile);

    void delete(Long id);
}

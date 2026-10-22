package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.Alert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Consultas de alertas que piden los otros módulos y microservicios. */
public interface ConsultarAlertasUseCase {

    Optional<Alert> find(Long id);

    List<Alert> findMany(Collection<Long> ids);

    List<Alert> all();

    /** Número de alertas por user_profile.id. */
    Map<Long, Long> countByProfile();

    long cuantasActivas();

    List<Long> idsDelPerfil(Long profile);

    /** De esas alertas, las que la usuaria marcó para conservar sus evidencias. */
    Set<Long> keepEvidence(Collection<Long> ids);
}

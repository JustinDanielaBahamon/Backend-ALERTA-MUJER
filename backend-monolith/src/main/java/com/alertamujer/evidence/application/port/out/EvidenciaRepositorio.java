package com.alertamujer.evidence.application.port.out;

import com.alertamujer.evidence.domain.model.Evidence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: dónde se guardan las evidencias (lo implementa el adaptador de persistencia). */
public interface EvidenciaRepositorio {

    Evidence guardar(Evidence evidencia);

    Optional<Evidence> buscar(Long id);

    List<Evidence> todas();

    /** Evidencias de esas alertas, de la más reciente a la más antigua. */
    List<Evidence> deLasAlertas(Collection<Long> alertIds);

    /** Evidencias de una alerta, de la más antigua a la más reciente. */
    List<Evidence> deLaAlerta(Long alertId);

    List<Evidence> guardadasAntesDe(LocalDateTime fecha);

    void eliminar(Evidence evidencia);

    void eliminarTodas(List<Evidence> evidencias);
}

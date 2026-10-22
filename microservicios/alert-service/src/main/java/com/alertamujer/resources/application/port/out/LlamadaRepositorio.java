package com.alertamujer.resources.application.port.out;

import com.alertamujer.resources.domain.model.ResourceCall;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las llamadas a los recursos. */
public interface LlamadaRepositorio {

    List<ResourceCall> all();

    Optional<ResourceCall> find(Long id);

    /** De la más reciente a la más antigua. */
    List<ResourceCall> dePerfil(Long profile);

    List<ResourceCall> deRecurso(Long recursoId);

    List<ResourceCall> deAlerta(Long alertId);

    ResourceCall save(ResourceCall llamada);

    void delete(Long id);
}

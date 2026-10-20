package com.alertamujer.resources.application.port.out;

import com.alertamujer.resources.domain.model.ResourceCall;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las llamadas a los recursos. */
public interface LlamadaRepositorio {

    List<ResourceCall> todas();

    Optional<ResourceCall> buscar(Long id);

    /** De la más reciente a la más antigua. */
    List<ResourceCall> dePerfil(Long perfil);

    List<ResourceCall> deRecurso(Long recursoId);

    List<ResourceCall> deAlerta(Long alertId);

    ResourceCall guardar(ResourceCall llamada);

    void eliminar(Long id);
}

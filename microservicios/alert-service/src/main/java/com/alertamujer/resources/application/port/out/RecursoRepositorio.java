package com.alertamujer.resources.application.port.out;

import com.alertamujer.resources.domain.model.EmergencyResource;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los recursos de ayuda. */
public interface RecursoRepositorio {

    List<EmergencyResource> todos();

    Optional<EmergencyResource> find(Long id);

    List<EmergencyResource> deCiudad(String ciudad);

    List<EmergencyResource> deTipo(String type);

    EmergencyResource save(EmergencyResource recurso);

    void delete(Long id);
}

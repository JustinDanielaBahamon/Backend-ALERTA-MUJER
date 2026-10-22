package com.alertamujer.resources.application.port.out;

import com.alertamujer.resources.domain.model.EmergencyResource;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los recursos de ayuda. */
public interface ResourceRepository {

    List<EmergencyResource> all();

    Optional<EmergencyResource> find(Long id);

    List<EmergencyResource> ofCity(String city);

    List<EmergencyResource> ofType(String type);

    EmergencyResource save(EmergencyResource resource);

    void delete(Long id);
}

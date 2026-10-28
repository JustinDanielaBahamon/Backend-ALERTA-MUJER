package com.alertamujer.resources.application.port.in;

import com.alertamujer.resources.domain.model.ResourceData;
import com.alertamujer.resources.domain.model.EmergencyResource;

import java.util.List;
import java.util.Optional;

/** Help resources: seen by any signed-in user and edited by the administration. */
public interface ResourcesUseCase {

    List<EmergencyResource> all();

    Optional<EmergencyResource> one(Long id);

    List<EmergencyResource> ofCity(String city);

    List<EmergencyResource> ofType(String type);

    EmergencyResource create(ResourceData data);

    EmergencyResource replace(Long id, ResourceData data);

    void delete(Long id);
}

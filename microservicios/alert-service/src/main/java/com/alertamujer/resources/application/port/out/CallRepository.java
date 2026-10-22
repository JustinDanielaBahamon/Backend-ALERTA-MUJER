package com.alertamujer.resources.application.port.out;

import com.alertamujer.resources.domain.model.ResourceCall;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las llamadas a los recursos. */
public interface CallRepository {

    List<ResourceCall> all();

    Optional<ResourceCall> find(Long id);

    /** De la más reciente a la más antigua. */
    List<ResourceCall> ofProfile(Long profile);

    List<ResourceCall> ofResource(Long resourceId);

    List<ResourceCall> ofAlert(Long alertId);

    ResourceCall save(ResourceCall call);

    void delete(Long id);
}

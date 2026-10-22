package com.alertamujer.resources.application.port.out;

import com.alertamujer.resources.domain.model.ResourceCall;

import java.util.List;
import java.util.Optional;

/** Output port: calls to resources. */
public interface CallRepository {

    List<ResourceCall> all();

    Optional<ResourceCall> find(Long id);

    /** Newest to oldest. */
    List<ResourceCall> ofProfile(Long profile);

    List<ResourceCall> ofResource(Long resourceId);

    List<ResourceCall> ofAlert(Long alertId);

    ResourceCall save(ResourceCall call);

    void delete(Long id);
}

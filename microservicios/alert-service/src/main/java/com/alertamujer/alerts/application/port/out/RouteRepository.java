package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.SavedRoute;

import java.util.List;
import java.util.Optional;

/** Output port: saved routes. */
public interface RouteRepository {

    /** Newest first. */
    List<SavedRoute> ofUser(Long profile);

    Optional<SavedRoute> find(Long id);

    long count(Long profile);

    long countStarred(Long profile);

    SavedRoute save(SavedRoute route);

    void delete(Long id);
}

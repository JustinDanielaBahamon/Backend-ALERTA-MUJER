package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.FrequentLocation;

import java.util.List;
import java.util.Optional;

/** Output port: frequent places. */
public interface FrequentPlaceRepository {

    List<FrequentLocation> all();

    List<FrequentLocation> activeOfUser(Long profile);

    Optional<FrequentLocation> find(Long id);

    FrequentLocation save(FrequentLocation place);

    void delete(Long id);
}

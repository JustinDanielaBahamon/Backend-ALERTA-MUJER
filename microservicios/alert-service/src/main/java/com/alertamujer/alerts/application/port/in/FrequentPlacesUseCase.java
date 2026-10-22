package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.PlaceData;
import com.alertamujer.alerts.domain.model.FrequentLocation;

import java.util.List;

/** The user's frequent places. */
public interface FrequentPlacesUseCase {

    List<FrequentLocation> activeOf(Long userId);

    FrequentLocation one(Long id, Long userId);

    FrequentLocation create(PlaceData data, Long userId);

    FrequentLocation update(Long id, PlaceData data, Long userId);

    void delete(Long id, Long userId);
}

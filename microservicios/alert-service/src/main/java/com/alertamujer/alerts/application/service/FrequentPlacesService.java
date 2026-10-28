package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.FrequentPlacesUseCase;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.FrequentPlaceRepository;
import com.alertamujer.alerts.domain.model.PlaceData;
import com.alertamujer.alerts.domain.model.FrequentLocation;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** The user's frequent places (home, work...). */
@Service
public class FrequentPlacesService implements FrequentPlacesUseCase {

    private final FrequentPlaceRepository repository;
    private final IdentityPort identity;

    public FrequentPlacesService(FrequentPlaceRepository repository, IdentityPort identity) {
        this.repository = repository;
        this.identity = identity;
    }

    @Override
    public List<FrequentLocation> activeOf(Long userId) {
        return repository.activeOfUser(identity.requiredProfile(userId));
    }

    @Override
    public FrequentLocation one(Long id, Long userId) {
        FrequentLocation place = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("FrequentLocation", id));
        if (!place.esDe(identity.requiredProfile(userId))) {
            throw new AccessDeniedException("No puedes acceder a ubicaciones frecuentes de otro usuario");
        }
        return place;
    }

    @Override
    @Transactional
    public FrequentLocation create(PlaceData data, Long userId) {
        // The owner comes from the JWT; any profile in the body is ignored (prevents IDOR)
        return repository.save(FrequentLocation.created(identity.requiredProfile(userId), data, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public FrequentLocation update(Long id, PlaceData data, Long userId) {
        FrequentLocation place = one(id, userId);
        place.update(data, LocalDateTime.now());
        return repository.save(place);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(one(id, userId).getId());
    }
}

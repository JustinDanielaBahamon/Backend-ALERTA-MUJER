package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NewRoute;
import com.alertamujer.alerts.application.port.in.RoutesUseCase;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.RouteRepository;
import com.alertamujer.alerts.domain.model.SavedRoute;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Saved routes: always of the token owner, never of another user. */
@Service
public class RoutesService implements RoutesUseCase {

    private final RouteRepository repository;
    private final IdentityPort identity;

    public RoutesService(RouteRepository repository, IdentityPort identity) {
        this.repository = repository;
        this.identity = identity;
    }

    @Override
    public List<SavedRoute> mine(Long userId) {
        return repository.ofUser(identity.requiredProfile(userId));
    }

    @Override
    @Transactional
    public SavedRoute create(NewRoute created, Long userId) {
        SavedRoute.validate(created.name(), created.points());
        Long profile = identity.requiredProfile(userId);
        SavedRoute.checkRoom(repository.count(profile));

        SavedRoute r = new SavedRoute();
        r.setUserProfileId(profile);
        r.setName(created.name().trim());
        r.setStartLabel(created.startLabel());
        r.setEndLabel(created.endLabel());
        r.setCity(created.city());
        r.setDepartment(created.department());
        r.setCountry(created.country());
        r.setDistanceKm(created.distanceKm());
        r.setEstimatedMinutes(created.estimatedMinutes());
        r.setPoints(created.points());
        r.setIsManual(created.isManual() == null || created.isManual());
        r.initialStar(repository.countStarred(profile));
        return repository.save(r);
    }

    @Override
    @Transactional
    public SavedRoute update(Long id, String name, Boolean starred, Long userId) {
        SavedRoute r = own(id, userId);
        r.rename(name);
        if (starred != null && !starred.equals(r.getIsStarred())) {
            r.markStarred(starred, repository.countStarred(r.getUserProfileId()));
        }
        return repository.save(r);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(own(id, userId).getId());
    }

    private SavedRoute own(Long id, Long userId) {
        SavedRoute r = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("SavedRoute", id));
        if (!r.esDe(identity.requiredProfile(userId))) {
            throw new AccessDeniedException("No puedes modificar recorridos de otro usuario");
        }
        return r;
    }
}

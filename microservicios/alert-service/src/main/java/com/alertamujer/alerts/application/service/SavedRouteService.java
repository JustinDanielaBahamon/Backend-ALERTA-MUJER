package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.RoutePoint;
import com.alertamujer.alerts.domain.model.SavedRoute;
import com.alertamujer.alerts.infrastructure.controller.dto.SavedRouteRequest;
import com.alertamujer.alerts.infrastructure.controller.dto.SavedRouteUpdateRequest;
import com.alertamujer.alerts.infrastructure.repository.SavedRouteRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ConflictException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SavedRouteService {

    // Reglas de negocio compartidas con web y móvil (también las garantiza un trigger en la base)
    public static final int MAX_ROUTES = 4;
    public static final int MAX_STARRED = 2;

    private final SavedRouteRepository savedRouteRepository;
    private final IdentityApi identityApi;

    public SavedRouteService(SavedRouteRepository savedRouteRepository, IdentityApi identityApi) {
        this.savedRouteRepository = savedRouteRepository;
        this.identityApi = identityApi;
    }

    // El JWT trae users.id; los recorridos se guardan por user_profile.id.
    private Long profileIdOf(Long userId) {
        return identityApi.requireProfileId(userId);
    }

    private SavedRoute getOwned(Long id, Long currentUserId) {
        SavedRoute route = savedRouteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavedRoute", id));
        if (!route.getUserProfileId().equals(profileIdOf(currentUserId))) {
            throw new AccessDeniedException("No puedes modificar recorridos de otro usuario");
        }
        return route;
    }

    public List<SavedRoute> getMine(Long currentUserId) {
        return savedRouteRepository.findByUserProfileIdOrderByCreatedAtDesc(profileIdOf(currentUserId));
    }

    @Transactional
    public SavedRoute create(SavedRouteRequest request, Long currentUserId) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El recorrido necesita un nombre");
        }
        List<RoutePoint> points = request.getPoints();
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("El recorrido necesita al menos un punto de inicio y uno de fin");
        }
        for (RoutePoint p : points) {
            if (p == null || p.latitude() == null || p.longitude() == null
                    || Math.abs(p.latitude()) > 90 || Math.abs(p.longitude()) > 180) {
                throw new IllegalArgumentException("Coordenadas inválidas en el recorrido");
            }
        }

        Long profileId = profileIdOf(currentUserId);
        if (savedRouteRepository.countByUserProfileId(profileId) >= MAX_ROUTES) {
            throw new ConflictException("Solo puedes tener " + MAX_ROUTES + " recorridos guardados. Elimina uno para crear otro.");
        }

        SavedRoute route = new SavedRoute();
        route.setUserProfileId(profileId);
        route.setName(request.getName().trim());
        route.setStartLabel(request.getStartLabel());
        route.setEndLabel(request.getEndLabel());
        route.setCity(request.getCity());
        route.setDepartment(request.getDepartment());
        route.setCountry(request.getCountry());
        route.setDistanceKm(request.getDistanceKm());
        route.setEstimatedMinutes(request.getEstimatedMinutes());
        route.setPoints(points);
        route.setIsManual(request.getIsManual() == null || request.getIsManual());
        // Igual que en el móvil: un recorrido nuevo es estrella mientras haya cupo
        route.setIsStarred(savedRouteRepository.countByUserProfileIdAndIsStarredTrue(profileId) < MAX_STARRED);
        return savedRouteRepository.save(route);
    }

    @Transactional
    public SavedRoute update(Long id, SavedRouteUpdateRequest changes, Long currentUserId) {
        SavedRoute route = getOwned(id, currentUserId);

        if (changes.getName() != null && !changes.getName().isBlank()) {
            route.setName(changes.getName().trim());
        }
        if (changes.getIsStarred() != null && !changes.getIsStarred().equals(route.getIsStarred())) {
            if (changes.getIsStarred()
                    && savedRouteRepository.countByUserProfileIdAndIsStarredTrue(route.getUserProfileId()) >= MAX_STARRED) {
                throw new ConflictException("Solo puedes tener " + MAX_STARRED + " recorridos estrella. Quita la estrella a otro para marcar este.");
            }
            route.setIsStarred(changes.getIsStarred());
        }
        return savedRouteRepository.save(route);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        savedRouteRepository.delete(getOwned(id, currentUserId));
    }
}

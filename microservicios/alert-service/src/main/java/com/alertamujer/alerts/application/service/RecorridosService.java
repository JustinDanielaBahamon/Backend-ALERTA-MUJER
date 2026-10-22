package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.NuevoRecorrido;
import com.alertamujer.alerts.application.port.in.RecorridosUseCase;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.RecorridoRepositorio;
import com.alertamujer.alerts.domain.model.SavedRoute;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Recorridos guardados: siempre de la dueña del token, nunca de otra usuaria. */
@Service
public class RecorridosService implements RecorridosUseCase {

    private final RecorridoRepositorio repository;
    private final IdentityPort identity;

    public RecorridosService(RecorridoRepositorio repository, IdentityPort identity) {
        this.repository = repository;
        this.identity = identity;
    }

    @Override
    public List<SavedRoute> mine(Long userId) {
        return repository.ofUser(identity.perfilRequerido(userId));
    }

    @Override
    @Transactional
    public SavedRoute create(NuevoRecorrido created, Long userId) {
        SavedRoute.validate(created.name(), created.points());
        Long profile = identity.perfilRequerido(userId);
        SavedRoute.verificarCupo(repository.count(profile));

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
        r.estrellaInicial(repository.contarEstrella(profile));
        return repository.save(r);
    }

    @Override
    @Transactional
    public SavedRoute update(Long id, String name, Boolean estrella, Long userId) {
        SavedRoute r = own(id, userId);
        r.rename(name);
        if (estrella != null && !estrella.equals(r.getIsStarred())) {
            r.marcarEstrella(estrella, repository.contarEstrella(r.getUserProfileId()));
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
        if (!r.esDe(identity.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes modificar recorridos de otro usuario");
        }
        return r;
    }
}

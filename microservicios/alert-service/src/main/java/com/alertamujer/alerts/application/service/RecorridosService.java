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
    private final IdentityPort identidad;

    public RecorridosService(RecorridoRepositorio repository, IdentityPort identidad) {
        this.repository = repository;
        this.identidad = identidad;
    }

    @Override
    public List<SavedRoute> mios(Long userId) {
        return repository.ofUser(identidad.perfilRequerido(userId));
    }

    @Override
    @Transactional
    public SavedRoute create(NuevoRecorrido nuevo, Long userId) {
        SavedRoute.validate(nuevo.name(), nuevo.points());
        Long perfil = identidad.perfilRequerido(userId);
        SavedRoute.verificarCupo(repository.contar(perfil));

        SavedRoute r = new SavedRoute();
        r.setUserProfileId(perfil);
        r.setName(nuevo.name().trim());
        r.setStartLabel(nuevo.startLabel());
        r.setEndLabel(nuevo.endLabel());
        r.setCity(nuevo.city());
        r.setDepartment(nuevo.department());
        r.setCountry(nuevo.country());
        r.setDistanceKm(nuevo.distanceKm());
        r.setEstimatedMinutes(nuevo.estimatedMinutes());
        r.setPoints(nuevo.points());
        r.setIsManual(nuevo.isManual() == null || nuevo.isManual());
        r.estrellaInicial(repository.contarEstrella(perfil));
        return repository.save(r);
    }

    @Override
    @Transactional
    public SavedRoute actualizar(Long id, String name, Boolean estrella, Long userId) {
        SavedRoute r = propio(id, userId);
        r.renombrar(name);
        if (estrella != null && !estrella.equals(r.getIsStarred())) {
            r.marcarEstrella(estrella, repository.contarEstrella(r.getUserProfileId()));
        }
        return repository.save(r);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(propio(id, userId).getId());
    }

    private SavedRoute propio(Long id, Long userId) {
        SavedRoute r = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("SavedRoute", id));
        if (!r.esDe(identidad.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes modificar recorridos de otro usuario");
        }
        return r;
    }
}

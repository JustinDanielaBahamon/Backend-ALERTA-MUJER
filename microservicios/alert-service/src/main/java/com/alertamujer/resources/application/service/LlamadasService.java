package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.in.LlamadasUseCase;
import com.alertamujer.resources.application.port.out.IdentityPort;
import com.alertamujer.resources.application.port.out.LlamadaRepositorio;
import com.alertamujer.resources.application.port.out.RecursoRepositorio;
import com.alertamujer.resources.domain.model.EmergencyResource;
import com.alertamujer.resources.domain.model.ResourceCall;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Llamadas a los recursos: cada usuaria solo ve y cambia las suyas. */
@Service
public class LlamadasService implements LlamadasUseCase {

    private final LlamadaRepositorio repository;
    private final RecursoRepositorio recursos;
    private final IdentityPort identity;

    public LlamadasService(LlamadaRepositorio repository, RecursoRepositorio recursos, IdentityPort identity) {
        this.repository = repository;
        this.recursos = recursos;
        this.identity = identity;
    }

    @Override
    public List<ResourceCall> all() {
        return repository.all();
    }

    @Override
    public Optional<ResourceCall> una(Long id, Long userId) {
        Long profile = identity.perfilRequerido(userId);
        return repository.find(id).filter(c -> c.esDe(profile));
    }

    @Override
    public List<ResourceCall> mias(Long userId) {
        return repository.dePerfil(identity.perfilRequerido(userId));
    }

    @Override
    public List<ResourceCall> deRecurso(Long recursoId) {
        return repository.deRecurso(recursoId);
    }

    @Override
    public List<ResourceCall> deAlerta(Long alertId) {
        return repository.deAlerta(alertId);
    }

    @Override
    @Transactional
    public ResourceCall registrar(Long recursoId, Long alertId, Long deviceId, String telefonoMarcado, String status, Long userId) {
        if (recursoId == null) throw new IllegalArgumentException("Indica a qué recurso se llamó");
        EmergencyResource recurso = recursos.find(recursoId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyResource", recursoId));
        return repository.save(ResourceCall.newOne(identity.perfilRequerido(userId), recurso, alertId, deviceId,
                telefonoMarcado, status, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public ResourceCall update(Long id, String status, LocalDateTime fin, Integer duracionSegundos, Long userId) {
        ResourceCall c = own(id, userId);
        c.update(status, fin, duracionSegundos);
        return repository.save(c);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(own(id, userId).getId());
    }

    private ResourceCall own(Long id, Long userId) {
        ResourceCall c = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("ResourceCall", id));
        if (!c.esDe(identity.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes acceder a llamadas de otro usuario");
        }
        return c;
    }
}

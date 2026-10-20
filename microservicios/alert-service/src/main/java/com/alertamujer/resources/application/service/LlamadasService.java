package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.in.LlamadasUseCase;
import com.alertamujer.resources.application.port.out.IdentidadPort;
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

    private final LlamadaRepositorio repositorio;
    private final RecursoRepositorio recursos;
    private final IdentidadPort identidad;

    public LlamadasService(LlamadaRepositorio repositorio, RecursoRepositorio recursos, IdentidadPort identidad) {
        this.repositorio = repositorio;
        this.recursos = recursos;
        this.identidad = identidad;
    }

    @Override
    public List<ResourceCall> todas() {
        return repositorio.todas();
    }

    @Override
    public Optional<ResourceCall> una(Long id, Long userId) {
        Long perfil = identidad.perfilRequerido(userId);
        return repositorio.buscar(id).filter(c -> c.esDe(perfil));
    }

    @Override
    public List<ResourceCall> mias(Long userId) {
        return repositorio.dePerfil(identidad.perfilRequerido(userId));
    }

    @Override
    public List<ResourceCall> deRecurso(Long recursoId) {
        return repositorio.deRecurso(recursoId);
    }

    @Override
    public List<ResourceCall> deAlerta(Long alertId) {
        return repositorio.deAlerta(alertId);
    }

    @Override
    @Transactional
    public ResourceCall registrar(Long recursoId, Long alertId, Long deviceId, String telefonoMarcado, String estado, Long userId) {
        if (recursoId == null) throw new IllegalArgumentException("Indica a qué recurso se llamó");
        EmergencyResource recurso = recursos.buscar(recursoId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyResource", recursoId));
        return repositorio.guardar(ResourceCall.nueva(identidad.perfilRequerido(userId), recurso, alertId, deviceId,
                telefonoMarcado, estado, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public ResourceCall actualizar(Long id, String estado, LocalDateTime fin, Integer duracionSegundos, Long userId) {
        ResourceCall c = propia(id, userId);
        c.actualizar(estado, fin, duracionSegundos);
        return repositorio.guardar(c);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long userId) {
        repositorio.eliminar(propia(id, userId).getId());
    }

    private ResourceCall propia(Long id, Long userId) {
        ResourceCall c = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("ResourceCall", id));
        if (!c.esDe(identidad.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes acceder a llamadas de otro usuario");
        }
        return c;
    }
}

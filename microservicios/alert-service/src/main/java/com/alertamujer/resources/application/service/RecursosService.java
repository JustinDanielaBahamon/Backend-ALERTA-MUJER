package com.alertamujer.resources.application.service;

import com.alertamujer.resources.application.port.in.RecursosUseCase;
import com.alertamujer.resources.application.port.out.RecursoRepositorio;
import com.alertamujer.resources.domain.model.DatosRecurso;
import com.alertamujer.resources.domain.model.EmergencyResource;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Recursos de ayuda. Solo conoce el puerto de los recursos. */
@Service
public class RecursosService implements RecursosUseCase {

    private final RecursoRepositorio repository;

    public RecursosService(RecursoRepositorio repository) {
        this.repository = repository;
    }

    @Override
    public List<EmergencyResource> todos() {
        return repository.todos();
    }

    @Override
    public Optional<EmergencyResource> uno(Long id) {
        return repository.find(id);
    }

    @Override
    public List<EmergencyResource> deCiudad(String ciudad) {
        return repository.deCiudad(ciudad);
    }

    @Override
    public List<EmergencyResource> deTipo(String type) {
        return repository.deTipo(type);
    }

    @Override
    @Transactional
    public EmergencyResource create(DatosRecurso data) {
        return repository.save(EmergencyResource.nuevo(data));
    }

    @Override
    @Transactional
    public EmergencyResource reemplazar(Long id, DatosRecurso data) {
        EmergencyResource r = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("EmergencyResource", id));
        r.reemplazar(data);
        return repository.save(r);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(id);
    }
}

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

    private final RecursoRepositorio repositorio;

    public RecursosService(RecursoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<EmergencyResource> todos() {
        return repositorio.todos();
    }

    @Override
    public Optional<EmergencyResource> uno(Long id) {
        return repositorio.buscar(id);
    }

    @Override
    public List<EmergencyResource> deCiudad(String ciudad) {
        return repositorio.deCiudad(ciudad);
    }

    @Override
    public List<EmergencyResource> deTipo(String tipo) {
        return repositorio.deTipo(tipo);
    }

    @Override
    @Transactional
    public EmergencyResource crear(DatosRecurso datos) {
        return repositorio.guardar(EmergencyResource.nuevo(datos));
    }

    @Override
    @Transactional
    public EmergencyResource reemplazar(Long id, DatosRecurso datos) {
        EmergencyResource r = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("EmergencyResource", id));
        r.reemplazar(datos);
        return repositorio.guardar(r);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repositorio.eliminar(id);
    }
}

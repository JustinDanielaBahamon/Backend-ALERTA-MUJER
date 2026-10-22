package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.in.ZonasUseCase;
import com.alertamujer.zones.application.port.out.ZonaRepositorio;
import com.alertamujer.zones.domain.model.DatosZona;
import com.alertamujer.zones.domain.model.Zone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Zonas del mapa. Solo conoce el puerto de las zonas. */
@Service
public class ZonasService implements ZonasUseCase {

    private final ZonaRepositorio repository;

    public ZonasService(ZonaRepositorio repository) {
        this.repository = repository;
    }

    @Override
    public List<Zone> todas() {
        return repository.todas();
    }

    @Override
    public Optional<Zone> una(Long id) {
        return repository.find(id);
    }

    @Override
    public List<Zone> deCiudad(String ciudad) {
        return repository.deCiudad(ciudad);
    }

    @Override
    public List<Zone> deTipo(String type) {
        return repository.deTipo(type);
    }

    @Override
    @Transactional
    public Zone create(DatosZona data) {
        return repository.save(Zone.nueva(data));
    }

    @Override
    @Transactional
    public Zone reemplazar(Long id, DatosZona data) {
        Zone zona = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Zone", id));
        zona.reemplazar(data);
        return repository.save(zona);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivas() {
        return repository.todas().stream().filter(Zone::activa).count();
    }
}

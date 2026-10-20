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

    private final ZonaRepositorio repositorio;

    public ZonasService(ZonaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Zone> todas() {
        return repositorio.todas();
    }

    @Override
    public Optional<Zone> una(Long id) {
        return repositorio.buscar(id);
    }

    @Override
    public List<Zone> deCiudad(String ciudad) {
        return repositorio.deCiudad(ciudad);
    }

    @Override
    public List<Zone> deTipo(String tipo) {
        return repositorio.deTipo(tipo);
    }

    @Override
    @Transactional
    public Zone crear(DatosZona datos) {
        return repositorio.guardar(Zone.nueva(datos));
    }

    @Override
    @Transactional
    public Zone reemplazar(Long id, DatosZona datos) {
        Zone zona = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Zone", id));
        zona.reemplazar(datos);
        return repositorio.guardar(zona);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repositorio.eliminar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivas() {
        return repositorio.todas().stream().filter(Zone::activa).count();
    }
}

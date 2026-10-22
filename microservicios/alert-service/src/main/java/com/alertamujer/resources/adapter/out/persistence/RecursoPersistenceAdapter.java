package com.alertamujer.resources.adapter.out.persistence;

import com.alertamujer.resources.application.port.out.RecursoRepositorio;
import com.alertamujer.resources.domain.model.EmergencyResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los recursos de ayuda en PostgreSQL. */
@Component
class RecursoPersistenceAdapter implements RecursoRepositorio {

    private final EmergencyResourceJpaRepository jpa;

    RecursoPersistenceAdapter(EmergencyResourceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<EmergencyResource> todos() {
        return jpa.findAll().stream().map(RecursoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<EmergencyResource> find(Long id) {
        return jpa.findById(id).map(RecursoPersistenceAdapter::aDominio);
    }

    @Override
    public List<EmergencyResource> deCiudad(String ciudad) {
        return jpa.findByCity(ciudad).stream().map(RecursoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<EmergencyResource> deTipo(String type) {
        return jpa.findByResourceType(type).stream().map(RecursoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public EmergencyResource save(EmergencyResource recurso) {
        return aDominio(jpa.save(aFila(recurso)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static EmergencyResource aDominio(EmergencyResourceJpaEntity f) {
        EmergencyResource d = new EmergencyResource();
        d.setId(f.getId());
        d.setName(f.getName());
        d.setResourceType(f.getResourceType());
        d.setTelephone(f.getTelephone());
        d.setSecondaryTelephone(f.getSecondaryTelephone());
        d.setEmail(f.getEmail());
        d.setAddress(f.getAddress());
        d.setCity(f.getCity());
        d.setLatitude(f.getLatitude());
        d.setLongitude(f.getLongitude());
        d.setDescription(f.getDescription());
        d.setIsActive(f.getIsActive());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static EmergencyResourceJpaEntity aFila(EmergencyResource d) {
        EmergencyResourceJpaEntity f = new EmergencyResourceJpaEntity();
        f.setId(d.getId());
        f.setName(d.getName());
        f.setResourceType(d.getResourceType());
        f.setTelephone(d.getTelephone());
        f.setSecondaryTelephone(d.getSecondaryTelephone());
        f.setEmail(d.getEmail());
        f.setAddress(d.getAddress());
        f.setCity(d.getCity());
        f.setLatitude(d.getLatitude());
        f.setLongitude(d.getLongitude());
        f.setDescription(d.getDescription());
        f.setIsActive(d.getIsActive());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}

package com.alertamujer.resources.adapter.out.persistence;

import com.alertamujer.resources.application.port.out.ResourceRepository;
import com.alertamujer.resources.domain.model.EmergencyResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los recursos de ayuda en PostgreSQL. */
@Component
class ResourcePersistenceAdapter implements ResourceRepository {

    private final EmergencyResourceJpaRepository jpa;

    ResourcePersistenceAdapter(EmergencyResourceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<EmergencyResource> all() {
        return jpa.findAll().stream().map(ResourcePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<EmergencyResource> find(Long id) {
        return jpa.findById(id).map(ResourcePersistenceAdapter::toDomain);
    }

    @Override
    public List<EmergencyResource> ofCity(String city) {
        return jpa.findByCity(city).stream().map(ResourcePersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<EmergencyResource> ofType(String type) {
        return jpa.findByResourceType(type).stream().map(ResourcePersistenceAdapter::toDomain).toList();
    }

    @Override
    public EmergencyResource save(EmergencyResource resource) {
        return toDomain(jpa.save(toRow(resource)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static EmergencyResource toDomain(EmergencyResourceJpaEntity f) {
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

    static EmergencyResourceJpaEntity toRow(EmergencyResource d) {
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

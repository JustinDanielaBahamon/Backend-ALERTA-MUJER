package com.alertamujer.resources.adapter.out.persistence;

import com.alertamujer.resources.application.port.out.LlamadaRepositorio;
import com.alertamujer.resources.domain.model.ResourceCall;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: las llamadas a los recursos en PostgreSQL. */
@Component
class LlamadaPersistenceAdapter implements LlamadaRepositorio {

    private final ResourceCallJpaRepository jpa;

    LlamadaPersistenceAdapter(ResourceCallJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ResourceCall> todas() {
        return jpa.findAll().stream().map(LlamadaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<ResourceCall> find(Long id) {
        return jpa.findById(id).map(LlamadaPersistenceAdapter::aDominio);
    }

    @Override
    public List<ResourceCall> dePerfil(Long perfil) {
        return jpa.findByUserProfileIdOrderByStartedAtDesc(perfil).stream().map(LlamadaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<ResourceCall> deRecurso(Long recursoId) {
        return jpa.findByEmergencyResourceId(recursoId).stream().map(LlamadaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<ResourceCall> deAlerta(Long alertId) {
        return jpa.findByAlertId(alertId).stream().map(LlamadaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public ResourceCall save(ResourceCall llamada) {
        return aDominio(jpa.save(aFila(llamada)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static ResourceCall aDominio(ResourceCallJpaEntity f) {
        ResourceCall d = new ResourceCall();
        d.setId(f.getId());
        d.setEmergencyResourceId(f.getEmergencyResourceId());
        d.setUserProfileId(f.getUserProfileId());
        d.setAlertId(f.getAlertId());
        d.setDeviceId(f.getDeviceId());
        d.setTelephoneDialed(f.getTelephoneDialed());
        d.setStatus(f.getStatus());
        d.setStartedAt(f.getStartedAt());
        d.setEndedAt(f.getEndedAt());
        d.setDurationSeconds(f.getDurationSeconds());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static ResourceCallJpaEntity aFila(ResourceCall d) {
        ResourceCallJpaEntity f = new ResourceCallJpaEntity();
        f.setId(d.getId());
        f.setEmergencyResourceId(d.getEmergencyResourceId());
        f.setUserProfileId(d.getUserProfileId());
        f.setAlertId(d.getAlertId());
        f.setDeviceId(d.getDeviceId());
        f.setTelephoneDialed(d.getTelephoneDialed());
        f.setStatus(d.getStatus());
        f.setStartedAt(d.getStartedAt());
        f.setEndedAt(d.getEndedAt());
        f.setDurationSeconds(d.getDurationSeconds());
        f.setCreatedAt(d.getCreatedAt());
        return f;
    }
}

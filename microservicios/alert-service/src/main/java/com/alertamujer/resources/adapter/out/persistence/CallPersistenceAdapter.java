package com.alertamujer.resources.adapter.out.persistence;

import com.alertamujer.resources.application.port.out.CallRepository;
import com.alertamujer.resources.domain.model.ResourceCall;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: calls to resources in PostgreSQL. */
@Component
class CallPersistenceAdapter implements CallRepository {

    private final ResourceCallJpaRepository jpa;

    CallPersistenceAdapter(ResourceCallJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ResourceCall> all() {
        return jpa.findAll().stream().map(CallPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<ResourceCall> find(Long id) {
        return jpa.findById(id).map(CallPersistenceAdapter::toDomain);
    }

    @Override
    public List<ResourceCall> ofProfile(Long profile) {
        return jpa.findByUserProfileIdOrderByStartedAtDesc(profile).stream().map(CallPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<ResourceCall> ofResource(Long resourceId) {
        return jpa.findByEmergencyResourceId(resourceId).stream().map(CallPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<ResourceCall> ofAlert(Long alertId) {
        return jpa.findByAlertId(alertId).stream().map(CallPersistenceAdapter::toDomain).toList();
    }

    @Override
    public ResourceCall save(ResourceCall call) {
        return toDomain(jpa.save(toRow(call)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static ResourceCall toDomain(ResourceCallJpaEntity f) {
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

    static ResourceCallJpaEntity toRow(ResourceCall d) {
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

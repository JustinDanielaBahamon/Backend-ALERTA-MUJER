package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.ContactRepository;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Output adapter: emergency contacts in PostgreSQL. */
@Component
class ContactPersistenceAdapter implements ContactRepository {

    private final EmergencyContactJpaRepository jpa;

    ContactPersistenceAdapter(EmergencyContactJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<EmergencyContact> all() {
        return jpa.findAll().stream().map(ContactPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<EmergencyContact> find(Long id) {
        return jpa.findById(id).map(ContactPersistenceAdapter::toDomain);
    }

    @Override
    public List<EmergencyContact> findSeveral(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return jpa.findAllById(ids).stream().map(ContactPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<EmergencyContact> ofUser(Long profile) {
        return jpa.findByUserProfileId(profile).stream().map(ContactPersistenceAdapter::toDomain).toList();
    }

    @Override
    public EmergencyContact save(EmergencyContact contact) {
        return toDomain(jpa.save(toRow(contact)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static EmergencyContact toDomain(EmergencyContactJpaEntity f) {
        EmergencyContact d = new EmergencyContact();
        d.setId(f.getId());
        d.setUserProfileId(f.getUserProfileId());
        d.setContactName(f.getContactName());
        d.setTelephone(f.getTelephone());
        d.setEmail(f.getEmail());
        d.setRelationship(f.getRelationship());
        d.setPhoto(f.getPhoto());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static EmergencyContactJpaEntity toRow(EmergencyContact d) {
        EmergencyContactJpaEntity f = new EmergencyContactJpaEntity();
        f.setId(d.getId());
        f.setUserProfileId(d.getUserProfileId());
        f.setContactName(d.getContactName());
        f.setTelephone(d.getTelephone());
        f.setEmail(d.getEmail());
        f.setRelationship(d.getRelationship());
        f.setPhoto(d.getPhoto());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}

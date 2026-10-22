package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.ContactoRepositorio;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los contactos de emergencia en PostgreSQL. */
@Component
class ContactoPersistenceAdapter implements ContactoRepositorio {

    private final EmergencyContactJpaRepository jpa;

    ContactoPersistenceAdapter(EmergencyContactJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<EmergencyContact> todos() {
        return jpa.findAll().stream().map(ContactoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public Optional<EmergencyContact> find(Long id) {
        return jpa.findById(id).map(ContactoPersistenceAdapter::aDominio);
    }

    @Override
    public List<EmergencyContact> buscarVarios(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return jpa.findAllById(ids).stream().map(ContactoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<EmergencyContact> ofUser(Long perfil) {
        return jpa.findByUserProfileId(perfil).stream().map(ContactoPersistenceAdapter::aDominio).toList();
    }

    @Override
    public EmergencyContact save(EmergencyContact contacto) {
        return aDominio(jpa.save(aFila(contacto)));
    }

    @Override
    public void delete(Long id) {
        jpa.deleteById(id);
    }

    static EmergencyContact aDominio(EmergencyContactJpaEntity f) {
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

    static EmergencyContactJpaEntity aFila(EmergencyContact d) {
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

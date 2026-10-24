package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.RecipientRepository;
import com.alertamujer.alerts.domain.model.AlertContact;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: the deliveries of each alert in PostgreSQL. */
@Component
class RecipientPersistenceAdapter implements RecipientRepository {

    private final AlertContactJpaRepository jpa;

    RecipientPersistenceAdapter(AlertContactJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void saveAll(List<AlertContact> recipients) {
        jpa.saveAll(recipients.stream().map(RecipientPersistenceAdapter::toRow).toList());
    }

    @Override
    public AlertContact save(AlertContact recipient) {
        return toDomain(jpa.save(toRow(recipient)));
    }

    @Override
    public Optional<AlertContact> find(Long id) {
        return jpa.findById(id).map(RecipientPersistenceAdapter::toDomain);
    }

    @Override
    public List<AlertContact> ofAlert(Long alertId) {
        return jpa.findByAlertId(alertId).stream().map(RecipientPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Long> alertsNotifiedTo(String email) {
        return jpa.findAlertIdsNotifiedTo(email);
    }

    static AlertContact toDomain(AlertContactJpaEntity f) {
        AlertContact d = new AlertContact();
        d.setId(f.getId());
        d.setAlertId(f.getAlertId());
        d.setEmergencyContactId(f.getEmergencyContactId());
        d.setChannel(f.getChannel());
        d.setDestination(f.getDestination());
        d.setStatus(f.getStatus());
        d.setSentAt(f.getSentAt());
        d.setDeliveredAt(f.getDeliveredAt());
        d.setSeenAt(f.getSeenAt());
        d.setAttempts(f.getAttempts());
        d.setErrorMessage(f.getErrorMessage());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static AlertContactJpaEntity toRow(AlertContact d) {
        AlertContactJpaEntity f = new AlertContactJpaEntity();
        f.setId(d.getId());
        f.setAlertId(d.getAlertId());
        f.setEmergencyContactId(d.getEmergencyContactId());
        f.setChannel(d.getChannel());
        f.setDestination(d.getDestination());
        f.setStatus(d.getStatus());
        f.setSentAt(d.getSentAt());
        f.setDeliveredAt(d.getDeliveredAt());
        f.setSeenAt(d.getSeenAt());
        f.setAttempts(d.getAttempts());
        f.setErrorMessage(d.getErrorMessage());
        f.setCreatedAt(d.getCreatedAt());
        return f;
    }
}

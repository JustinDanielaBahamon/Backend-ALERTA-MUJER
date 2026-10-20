package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.DestinatarioRepositorio;
import com.alertamujer.alerts.domain.model.AlertContact;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los envíos de cada alerta en PostgreSQL. */
@Component
class DestinatarioPersistenceAdapter implements DestinatarioRepositorio {

    private final AlertContactJpaRepository jpa;

    DestinatarioPersistenceAdapter(AlertContactJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardarTodos(List<AlertContact> destinatarios) {
        jpa.saveAll(destinatarios.stream().map(DestinatarioPersistenceAdapter::aFila).toList());
    }

    @Override
    public AlertContact guardar(AlertContact destinatario) {
        return aDominio(jpa.save(aFila(destinatario)));
    }

    @Override
    public Optional<AlertContact> buscar(Long id) {
        return jpa.findById(id).map(DestinatarioPersistenceAdapter::aDominio);
    }

    @Override
    public List<AlertContact> deLaAlerta(Long alertId) {
        return jpa.findByAlertId(alertId).stream().map(DestinatarioPersistenceAdapter::aDominio).toList();
    }

    @Override
    public List<Long> alertasAvisadasA(String correo) {
        return jpa.findAlertIdsNotifiedTo(correo);
    }

    static AlertContact aDominio(AlertContactJpaEntity f) {
        AlertContact d = new AlertContact();
        d.setId(f.getId());
        d.setAlertId(f.getAlertId());
        d.setEmergencyContactId(f.getEmergencyContactId());
        d.setChannel(f.getChannel());
        d.setDestination(f.getDestination());
        d.setStatus(f.getStatus());
        d.setSentAt(f.getSentAt());
        d.setDeliveredAt(f.getDeliveredAt());
        d.setAttempts(f.getAttempts());
        d.setErrorMessage(f.getErrorMessage());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static AlertContactJpaEntity aFila(AlertContact d) {
        AlertContactJpaEntity f = new AlertContactJpaEntity();
        f.setId(d.getId());
        f.setAlertId(d.getAlertId());
        f.setEmergencyContactId(d.getEmergencyContactId());
        f.setChannel(d.getChannel());
        f.setDestination(d.getDestination());
        f.setStatus(d.getStatus());
        f.setSentAt(d.getSentAt());
        f.setDeliveredAt(d.getDeliveredAt());
        f.setAttempts(d.getAttempts());
        f.setErrorMessage(d.getErrorMessage());
        f.setCreatedAt(d.getCreatedAt());
        return f;
    }
}

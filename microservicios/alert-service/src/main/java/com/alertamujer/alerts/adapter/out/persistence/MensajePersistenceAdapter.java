package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.MensajeRepositorio;
import com.alertamujer.alerts.domain.model.AlertMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida: el chat de las alertas en PostgreSQL. */
@Component
class MensajePersistenceAdapter implements MensajeRepositorio {

    private final AlertMessageJpaRepository jpa;

    MensajePersistenceAdapter(AlertMessageJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<AlertMessage> posteriores(Long alertId, long afterId) {
        return jpa.findByAlertIdAndIdGreaterThanOrderByIdAsc(alertId, afterId).stream()
                .map(MensajePersistenceAdapter::aDominio).toList();
    }

    @Override
    public long contar(Long alertId) {
        return jpa.countByAlertId(alertId);
    }

    @Override
    public AlertMessage guardar(AlertMessage mensaje) {
        return aDominio(jpa.save(aFila(mensaje)));
    }

    static AlertMessage aDominio(AlertMessageJpaEntity f) {
        AlertMessage d = new AlertMessage();
        d.setId(f.getId());
        d.setAlertId(f.getAlertId());
        d.setSenderType(f.getSenderType());
        d.setSenderName(f.getSenderName());
        d.setBody(f.getBody());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static AlertMessageJpaEntity aFila(AlertMessage d) {
        AlertMessageJpaEntity f = new AlertMessageJpaEntity();
        f.setId(d.getId());
        f.setAlertId(d.getAlertId());
        f.setSenderType(d.getSenderType());
        f.setSenderName(d.getSenderName());
        f.setBody(d.getBody());
        f.setCreatedAt(d.getCreatedAt());
        return f;
    }
}

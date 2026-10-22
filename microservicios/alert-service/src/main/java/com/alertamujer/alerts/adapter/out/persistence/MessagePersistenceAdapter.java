package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.MessageRepository;
import com.alertamujer.alerts.domain.model.AlertMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adaptador de salida: el chat de las alertas en PostgreSQL. */
@Component
class MessagePersistenceAdapter implements MessageRepository {

    private final AlertMessageJpaRepository jpa;

    MessagePersistenceAdapter(AlertMessageJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<AlertMessage> posteriores(Long alertId, long afterId) {
        return jpa.findByAlertIdAndIdGreaterThanOrderByIdAsc(alertId, afterId).stream()
                .map(MessagePersistenceAdapter::toDomain).toList();
    }

    @Override
    public long count(Long alertId) {
        return jpa.countByAlertId(alertId);
    }

    @Override
    public AlertMessage save(AlertMessage message) {
        return toDomain(jpa.save(toRow(message)));
    }

    static AlertMessage toDomain(AlertMessageJpaEntity f) {
        AlertMessage d = new AlertMessage();
        d.setId(f.getId());
        d.setAlertId(f.getAlertId());
        d.setSenderType(f.getSenderType());
        d.setSenderName(f.getSenderName());
        d.setBody(f.getBody());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static AlertMessageJpaEntity toRow(AlertMessage d) {
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

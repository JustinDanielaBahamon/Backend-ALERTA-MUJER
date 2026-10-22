package com.alertamujer.notifications.adapter.out.persistence;

import com.alertamujer.notifications.application.port.out.VistasRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Adaptador de salida: las visitas al seguimiento en notification.tracking_view. */
@Component
class VistasJdbcAdapter implements VistasRepositorio {

    private final JdbcTemplate jdbc;

    VistasJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void registrar(Long alertId, String visitante, LocalDateTime now) {
        Timestamp t = Timestamp.valueOf(now);
        jdbc.update("INSERT INTO notification.tracking_view (alert_id, visitor, first_seen, last_seen) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (alert_id, visitor) DO UPDATE SET last_seen = EXCLUDED.last_seen", alertId, visitante, t, t);
    }

    @Override
    public Conteo count(Long alertId, LocalDateTime recientes) {
        return jdbc.queryForObject("SELECT count(*), count(*) FILTER (WHERE last_seen >= ?) "
                        + "FROM notification.tracking_view WHERE alert_id = ?",
                (rs, n) -> new Conteo(rs.getInt(1), rs.getInt(2)), Timestamp.valueOf(recientes), alertId);
    }
}

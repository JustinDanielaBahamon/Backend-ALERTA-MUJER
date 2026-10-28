package com.alertamujer.notifications.adapter.out.persistence;

import com.alertamujer.notifications.application.port.out.ViewsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Output adapter: tracking visits in notification.tracking_view. */
@Component
class ViewsJdbcAdapter implements ViewsRepository {

    private final JdbcTemplate jdbc;

    ViewsJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void registrar(Long alertId, String visitor, LocalDateTime now) {
        Timestamp t = Timestamp.valueOf(now);
        jdbc.update("INSERT INTO notification.tracking_view (alert_id, visitor, first_seen, last_seen) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (alert_id, visitor) DO UPDATE SET last_seen = EXCLUDED.last_seen", alertId, visitor, t, t);
    }

    @Override
    public ViewCount count(Long alertId, LocalDateTime recent) {
        return jdbc.queryForObject("SELECT count(*), count(*) FILTER (WHERE last_seen >= ?) "
                        + "FROM notification.tracking_view WHERE alert_id = ?",
                (rs, n) -> new ViewCount(rs.getInt(1), rs.getInt(2)), Timestamp.valueOf(recent), alertId);
    }
}

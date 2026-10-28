package com.alertamujer.notifications.adapter.out.persistence;

import com.alertamujer.notifications.application.port.out.EscalationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/** Output adapter: alert.alert.escalated_at and the visits in notification.tracking_view. */
@Component
class EscalationJdbcAdapter implements EscalationRepository {

    private final JdbcTemplate jdbc;

    EscalationJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<UnseenAlert> unseen(LocalDateTime startedAfter, LocalDateTime startedBefore) {
        return jdbc.query("SELECT a.id, a.user_profile_id, a.started_at FROM alert.alert a "
                        + "WHERE a.status = 'active' AND a.escalated_at IS NULL AND a.started_at > ? AND a.started_at <= ? "
                        + "AND NOT EXISTS (SELECT 1 FROM notification.tracking_view v WHERE v.alert_id = a.id) "
                        + "ORDER BY a.started_at",
                (rs, n) -> new UnseenAlert(rs.getLong(1), rs.getLong(2), rs.getTimestamp(3).toLocalDateTime()),
                Timestamp.valueOf(startedAfter), Timestamp.valueOf(startedBefore));
    }

    @Override
    public boolean claim(Long alertId, LocalDateTime now) {
        return jdbc.update("UPDATE alert.alert SET escalated_at = ? WHERE id = ? AND escalated_at IS NULL",
                Timestamp.valueOf(now), alertId) == 1;
    }
}

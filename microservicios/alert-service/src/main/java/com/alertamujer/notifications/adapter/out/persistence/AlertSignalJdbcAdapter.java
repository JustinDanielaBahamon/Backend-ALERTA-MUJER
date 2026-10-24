package com.alertamujer.notifications.adapter.out.persistence;

import com.alertamujer.notifications.application.port.out.AlertSignalRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output adapter: last signal and warnings of the alerts, in alert.alert (targeted updates). */
@Component
class AlertSignalJdbcAdapter implements AlertSignalRepository {

    // Her last signal: the last location received, or the start if none arrived yet
    private static final String LAST_SIGNAL = "COALESCE(a.last_signal_at, a.started_at)";

    private final JdbcTemplate jdbc;

    AlertSignalJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<SilentAlert> silent(LocalDateTime silentSince, LocalDateTime startedAfter) {
        return jdbc.query("SELECT a.id, a.user_profile_id, " + LAST_SIGNAL + " FROM alert.alert a "
                        + "WHERE a.status = 'active' AND a.started_at > ? AND " + LAST_SIGNAL + " <= ? "
                        + "AND a.activation_method NOT IN ('timer', 'signal_lost') "
                        + "AND (a.signal_lost_at IS NULL OR a.signal_lost_at < " + LAST_SIGNAL + ")",
                (rs, n) -> new SilentAlert(rs.getLong(1), rs.getLong(2), rs.getTimestamp(3).toLocalDateTime()),
                Timestamp.valueOf(startedAfter), Timestamp.valueOf(silentSince));
    }

    @Override
    public boolean claim(Long alertId, LocalDateTime now) {
        return jdbc.update("UPDATE alert.alert a SET signal_lost_at = ? WHERE a.id = ? "
                + "AND (a.signal_lost_at IS NULL OR a.signal_lost_at < " + LAST_SIGNAL + ")",
                Timestamp.valueOf(now), alertId) == 1;
    }

    @Override
    public Optional<String> currentWarning(Long alertId) {
        // Duress first; then a signal lost that has not come back; then unanswered
        List<String> found = jdbc.query("SELECT CASE "
                        + "WHEN a.duress_at IS NOT NULL THEN 'duress' "
                        + "WHEN a.signal_lost_at IS NOT NULL AND a.signal_lost_at >= " + LAST_SIGNAL + " THEN 'signal_lost' "
                        + "WHEN a.unanswered_at IS NOT NULL THEN 'unanswered' END "
                        + "FROM alert.alert a WHERE a.id = ? AND a.status = 'active'",
                (rs, n) -> rs.getString(1), alertId);
        return found.stream().filter(w -> w != null).findFirst();
    }
}

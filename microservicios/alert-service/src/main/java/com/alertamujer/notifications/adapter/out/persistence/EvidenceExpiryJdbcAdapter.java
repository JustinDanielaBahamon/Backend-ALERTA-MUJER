package com.alertamujer.notifications.adapter.out.persistence;

import com.alertamujer.notifications.application.port.out.EvidenceExpiryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/** Output adapter: alert.evidence of the alerts not kept, with the owner's user (identity.user_profile). */
@Component
class EvidenceExpiryJdbcAdapter implements EvidenceExpiryRepository {

    private final JdbcTemplate jdbc;

    EvidenceExpiryJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<UserWithExpiring> recordedBetween(LocalDateTime from, LocalDateTime to) {
        return jdbc.query("SELECT p.user_id, COUNT(DISTINCT a.id) FROM alert.evidence e "
                        + "JOIN alert.alert a ON a.id = e.alert_id "
                        + "JOIN identity.user_profile p ON p.id = a.user_profile_id "
                        + "WHERE a.keep_evidence = FALSE AND e.created_at >= ? AND e.created_at < ? "
                        + "GROUP BY p.user_id",
                (rs, n) -> new UserWithExpiring(rs.getLong(1), rs.getInt(2)),
                Timestamp.valueOf(from), Timestamp.valueOf(to));
    }
}

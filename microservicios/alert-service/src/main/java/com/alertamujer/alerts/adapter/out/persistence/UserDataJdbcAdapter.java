package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.UserDataRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output adapter: deletes in a single transaction everything personal of a user in the
 * Alert Service tables (alert, notification and resource). Alerts are not deleted: the evidence
 * (Evidence Service) and the deliveries point to them.
 */
@Component
class UserDataJdbcAdapter implements UserDataRepository {

    private static final String HER_ALERTS = "(SELECT id FROM alert.alert WHERE user_profile_id = ?)";

    private final JdbcTemplate jdbc;

    UserDataJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public int deleteOf(Long p) {
        int rows = 0;
        rows += jdbc.update("DELETE FROM alert.safe_trip WHERE user_profile_id = ?", p);
        rows += jdbc.update("DELETE FROM alert.location_log WHERE user_profile_id = ?", p);
        rows += jdbc.update("DELETE FROM alert.saved_route WHERE user_profile_id = ?", p);
        rows += jdbc.update("DELETE FROM alert.frequent_location WHERE user_profile_id = ?", p);
        rows += jdbc.update("DELETE FROM alert.alert_message WHERE alert_id IN " + HER_ALERTS, p);
        rows += jdbc.update("DELETE FROM notification.tracking_link WHERE alert_id IN " + HER_ALERTS, p);
        rows += jdbc.update("DELETE FROM notification.tracking_view WHERE alert_id IN " + HER_ALERTS, p);
        // Deliveries stay in the history without each contact's email or token
        rows += jdbc.update("UPDATE alert.alert_contact SET destination = '(eliminado)', error_message = NULL "
                + "WHERE alert_id IN " + HER_ALERTS, p);
        // The alert_contact foreign key leaves the delivery without a contact (ON DELETE SET NULL)
        rows += jdbc.update("DELETE FROM alert.emergency_contact WHERE user_profile_id = ?", p);
        rows += jdbc.update("DELETE FROM resource.resource_call WHERE user_profile_id = ?", p);
        rows += jdbc.update("UPDATE alert.alert SET message = NULL, latitude = NULL, longitude = NULL, "
                + "keep_evidence = false WHERE user_profile_id = ?", p);
        return rows;
    }
}

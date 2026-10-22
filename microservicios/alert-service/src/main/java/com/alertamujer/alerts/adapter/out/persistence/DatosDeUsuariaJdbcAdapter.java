package com.alertamujer.alerts.adapter.out.persistence;

import com.alertamujer.alerts.application.port.out.DatosDeUsuariaRepositorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida: borra en una sola transacción todo lo personal de una usuaria en las tablas
 * de Alert Service (alert, notification y resource). Las alertas no se borran: las evidencias
 * (Evidence Service) y los envíos apuntan a ellas.
 */
@Component
class DatosDeUsuariaJdbcAdapter implements DatosDeUsuariaRepositorio {

    private static final String SUS_ALERTAS = "(SELECT id FROM alert.alert WHERE user_profile_id = ?)";

    private final JdbcTemplate jdbc;

    DatosDeUsuariaJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public int borrarDe(Long p) {
        int filas = 0;
        filas += jdbc.update("DELETE FROM alert.safe_trip WHERE user_profile_id = ?", p);
        filas += jdbc.update("DELETE FROM alert.location_log WHERE user_profile_id = ?", p);
        filas += jdbc.update("DELETE FROM alert.saved_route WHERE user_profile_id = ?", p);
        filas += jdbc.update("DELETE FROM alert.frequent_location WHERE user_profile_id = ?", p);
        filas += jdbc.update("DELETE FROM alert.alert_message WHERE alert_id IN " + SUS_ALERTAS, p);
        filas += jdbc.update("DELETE FROM notification.tracking_link WHERE alert_id IN " + SUS_ALERTAS, p);
        // Los envíos quedan en el historial sin el correo ni el token de cada contacto
        filas += jdbc.update("UPDATE alert.alert_contact SET destination = '(eliminado)', error_message = NULL "
                + "WHERE alert_id IN " + SUS_ALERTAS, p);
        // La llave foránea de alert_contact deja el envío sin contacto (ON DELETE SET NULL)
        filas += jdbc.update("DELETE FROM alert.emergency_contact WHERE user_profile_id = ?", p);
        filas += jdbc.update("DELETE FROM resource.resource_call WHERE user_profile_id = ?", p);
        filas += jdbc.update("UPDATE alert.alert SET message = NULL, latitude = NULL, longitude = NULL, "
                + "keep_evidence = false WHERE user_profile_id = ?", p);
        return filas;
    }
}

package com.alertamujer.admin.adapter.out.infraestructura;

import com.alertamujer.admin.application.port.out.InfraestructuraPort;
import com.google.firebase.FirebaseApp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Adaptador de salida: consulta la base de datos, Firebase y la JVM. */
@Component
class InfraestructuraAdapter implements InfraestructuraPort {

    private final JdbcTemplate jdbcTemplate;

    InfraestructuraAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean baseDeDatosResponde() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean pushConfigurado() {
        return !FirebaseApp.getApps().isEmpty();
    }

    @Override
    public LocalDateTime arranque() {
        long ms = ManagementFactory.getRuntimeMXBean().getStartTime();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
    }

    @Override
    public int versionJava() {
        return Runtime.version().feature();
    }
}

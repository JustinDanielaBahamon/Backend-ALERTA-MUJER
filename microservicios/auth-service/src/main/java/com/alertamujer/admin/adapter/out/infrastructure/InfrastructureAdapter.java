package com.alertamujer.admin.adapter.out.infrastructure;

import com.alertamujer.admin.application.port.out.InfrastructurePort;
import com.google.firebase.FirebaseApp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Output adapter: queries the database, Firebase and the JVM. */
@Component
class InfrastructureAdapter implements InfrastructurePort {

    private final JdbcTemplate jdbcTemplate;

    InfrastructureAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean databaseResponds() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean pushConfigured() {
        return !FirebaseApp.getApps().isEmpty();
    }

    @Override
    public LocalDateTime startup() {
        long ms = ManagementFactory.getRuntimeMXBean().getStartTime();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
    }

    @Override
    public int versionJava() {
        return Runtime.version().feature();
    }
}

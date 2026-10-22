package com.alertamujer.alerts.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlertContactTest {

    @Test
    void unEnvioNuevoQuedaPendiente() {
        AlertContact d = AlertContact.created(5L, 1L, AlertContact.CANAL_CORREO, "eva@correo.com");

        assertEquals("pending", d.getStatus());
        assertEquals(0, d.getAttempts());
    }

    @Test
    void registraCadaIntentoYCortaElError() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 20, 10, 0);
        AlertContact d = AlertContact.created(5L, 1L, AlertContact.CANAL_CORREO, "eva@correo.com");

        d.registrarIntento("failed", "x".repeat(800), now);
        assertEquals(1, d.getAttempts());
        assertEquals(500, d.getErrorMessage().length());
        assertNull(d.getSentAt());

        d.registrarIntento("sent", null, now);
        assertEquals(2, d.getAttempts());
        assertEquals(now, d.getSentAt());
        assertNull(d.getErrorMessage());
    }

    @Test
    void unEnvioAbiertoDeUnaAlertaTerminadaSeVeComoVencido() {
        AlertContact pending = AlertContact.created(5L, 1L, AlertContact.CANAL_PUSH, "App");
        AlertContact fallido = AlertContact.created(5L, 1L, AlertContact.CANAL_CORREO, "eva@correo.com");
        fallido.setStatus("failed");

        assertEquals("pending", pending.estadoVisible(false));
        assertEquals("expired", pending.estadoVisible(true));
        assertEquals("failed", fallido.estadoVisible(true));
    }
}

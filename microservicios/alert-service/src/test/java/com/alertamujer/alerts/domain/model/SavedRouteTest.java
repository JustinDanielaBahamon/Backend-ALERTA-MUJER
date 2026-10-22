package com.alertamujer.alerts.domain.model;

import com.alertamujer.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SavedRouteTest {

    private static final RoutePoint A = new RoutePoint(4.6, -74.1, null);
    private static final RoutePoint B = new RoutePoint(4.7, -74.0, null);

    @Test
    void necesitaNombreYAlMenosDosPuntosValidos() {
        assertDoesNotThrow(() -> SavedRoute.validate("Casa", List.of(A, B)));
        assertThrows(IllegalArgumentException.class, () -> SavedRoute.validate(" ", List.of(A, B)));
        assertThrows(IllegalArgumentException.class, () -> SavedRoute.validate("Casa", List.of(A)));
        assertThrows(IllegalArgumentException.class,
                () -> SavedRoute.validate("Casa", List.of(A, new RoutePoint(95.0, 0.0, null))));
    }

    @Test
    void maximo4RecorridosYUnoNuevoEsEstrellaMientrasHayaCupo() {
        assertDoesNotThrow(() -> SavedRoute.verificarCupo(3));
        assertThrows(ConflictException.class, () -> SavedRoute.verificarCupo(SavedRoute.MAX_ROUTES));

        SavedRoute r = new SavedRoute();
        r.estrellaInicial(1);
        assertTrue(r.getIsStarred());
        r.estrellaInicial(SavedRoute.MAX_STARRED);
        assertFalse(r.getIsStarred());
    }

    @Test
    void marcarEstrellaRespetaElMaximo() {
        SavedRoute r = new SavedRoute();

        assertThrows(ConflictException.class, () -> r.marcarEstrella(true, SavedRoute.MAX_STARRED));
        r.marcarEstrella(true, 1);
        assertTrue(r.getIsStarred());
        r.marcarEstrella(false, SavedRoute.MAX_STARRED);
        assertFalse(r.getIsStarred());
    }

    @Test
    void renombrarIgnoraUnNombreVacio() {
        SavedRoute r = new SavedRoute();
        r.setName("Casa");

        r.rename("  ");
        assertEquals("Casa", r.getName());
        r.rename(" Trabajo ");
        assertEquals("Trabajo", r.getName());
    }
}

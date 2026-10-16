package com.alertamujer.notifications.application;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.notifications.infrastructure.repository.TrackingLinkRepository;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingLinkServiceTest {

    @Mock
    private TrackingLinkRepository repository;

    @Mock
    private AlertsApi alertsApi;

    @Mock
    private IdentityApi identityApi;

    @Mock
    private com.alertamujer.settings.api.SettingsApi settingsApi;

    @InjectMocks
    private TrackingLinkService service;

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 16, 12, 0);
    private static final String CODIGO = "c0d1g0-de-prueba-suficientemente-largo";

    private AlertSummary alerta(String estado, LocalDateTime fin) {
        return new AlertSummary(5L, 10L, "main", "panic_button", estado, null, AHORA.minusHours(1),
                new BigDecimal("2.93"), new BigDecimal("-75.28"), fin, null);
    }

    private void conEnlace(LocalDateTime revocado) {
        TrackingLink link = new TrackingLink();
        link.setAlertId(5L);
        link.setRevokedAt(revocado);
        when(repository.findByTokenHash(TrackingLinkService.hash(CODIGO))).thenReturn(Optional.of(link));
    }

    @Test
    void crear_guardaSoloElHashDelCodigo() {
        String codigo = service.crear(5L);

        ArgumentCaptor<TrackingLink> captor = ArgumentCaptor.forClass(TrackingLink.class);
        verify(repository).save(captor.capture());
        assertTrue(codigo.length() >= 40);
        assertEquals(64, captor.getValue().getTokenHash().length());
        assertNotEquals(codigo, captor.getValue().getTokenHash());
    }

    @Test
    void consultar_alertaActivaMuestraNombreDePilaYUltimaUbicacion() {
        conEnlace(null);
        when(alertsApi.findAlert(5L)).thenReturn(Optional.of(alerta("active", null)));
        when(identityApi.findUserByProfileId(10L)).thenReturn(Optional.of(new UserSummary(1L, "Ana", "Gómez", "a@b.co", "300", 1L, null)));
        when(alertsApi.locationTrail(5L)).thenReturn(List.of());
        when(settingsApi.numeroEmergencia()).thenReturn("123");

        TrackingLinkService.TrackingView vista = service.consultar(CODIGO, AHORA);

        assertEquals("Ana", vista.firstName());
        assertEquals("123", vista.emergencyNumber());
        assertTrue(vista.active());
        assertEquals(new BigDecimal("2.93"), vista.lastPoint().latitude()); // sin recorrido aún: punto de activación
    }

    @Test
    void consultar_venceDosHorasDespuesDeTerminar() {
        conEnlace(null);
        when(alertsApi.findAlert(5L)).thenReturn(Optional.of(alerta("resolved", AHORA.minusHours(3))));

        assertThrows(ResourceNotFoundException.class, () -> service.consultar(CODIGO, AHORA));
    }

    @Test
    void consultar_rechazaEnlacesRevocadosOInexistentes() {
        conEnlace(AHORA);
        assertThrows(ResourceNotFoundException.class, () -> service.consultar(CODIGO, AHORA));
        assertThrows(ResourceNotFoundException.class, () -> service.consultar("corto", AHORA));
    }
}

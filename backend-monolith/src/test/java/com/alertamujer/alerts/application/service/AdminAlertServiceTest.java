package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private IdentityApi identityApi;


    @InjectMocks
    private AdminAlertService service;

    private Alert alerta(Long id, Long perfilId, LocalDateTime inicio) {
        Alert a = new Alert();
        a.setId(id);
        a.setUserProfileId(perfilId);
        a.setActivationMethod("panic_button");
        a.setStartedAt(inicio);
        return a;
    }

    @Test
    void listar_agregaLaUsuariaYOrdenaDeLaMasReciente() {
        UserSummary usuaria = new UserSummary(1L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);

        LocalDateTime ahora = LocalDateTime.now();
        when(alertRepository.findAll()).thenReturn(List.of(
                alerta(1L, 10L, ahora.minusHours(2)),
                alerta(2L, 99L, ahora)));
        when(identityApi.usersByProfileId()).thenReturn(java.util.Map.of(10L, usuaria));

        List<AdminAlertView> vistas = service.listar();

        assertEquals(2L, vistas.get(0).id());
        assertNull(vistas.get(0).userName());
        assertEquals("Ana Gómez", vistas.get(1).userName());
        assertEquals("ana@correo.com", vistas.get(1).userEmail());
        assertEquals(1L, vistas.get(1).userId());
        assertEquals("panic_button", vistas.get(1).activationMethod());
    }

    @Test
    void cambiarEstado_resolvedMarcaLaHoraDeCierre() {
        Alert alert = alerta(5L, 10L, LocalDateTime.now());
        when(alertRepository.findById(5L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert resultado = service.cambiarEstado(5L, "resolved");

        assertEquals("resolved", resultado.getStatus());
        assertNotNull(resultado.getEndedAt());
    }

    @Test
    void cambiarEstado_rechazaEstadosDesconocidos() {
        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(5L, "Atendida"));
    }
}

package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

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
        UserProfile perfil = new UserProfile();
        perfil.setId(10L);
        perfil.setUserId(1L);
        User usuaria = new User();
        usuaria.setId(1L);
        usuaria.setFirstName("Ana");
        usuaria.setLastName("Gómez");
        usuaria.setEmail("ana@correo.com");

        LocalDateTime ahora = LocalDateTime.now();
        when(alertRepository.findAll()).thenReturn(List.of(
                alerta(1L, 10L, ahora.minusHours(2)),
                alerta(2L, 99L, ahora)));
        when(userProfileRepository.findAll()).thenReturn(List.of(perfil));
        when(userRepository.findAll()).thenReturn(List.of(usuaria));

        List<AdminAlertService.AdminAlertView> vistas = service.listar();

        assertEquals(2L, vistas.get(0).id());
        assertNull(vistas.get(0).userName());
        assertEquals("Ana Gómez", vistas.get(1).userName());
        assertEquals("ana@correo.com", vistas.get(1).userEmail());
        assertEquals(1L, vistas.get(1).userId());
        assertEquals("panic_button", vistas.get(1).activationMethod());
    }
}

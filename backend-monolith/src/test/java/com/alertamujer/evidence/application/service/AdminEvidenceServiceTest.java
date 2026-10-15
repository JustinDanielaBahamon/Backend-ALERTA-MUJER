package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.evidence.infrastructure.repository.EvidenceRepository;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEvidenceServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminEvidenceService service;

    private Evidence evidencia(Long id, String revision, long tamanio) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(5L);
        e.setMediaType("photo");
        e.setFileName("foto.jpg");
        e.setSizeBytes(tamanio);
        e.setStorageKey("clave-" + id);
        e.setReviewStatus(revision);
        return e;
    }

    @Test
    void listar_agregaUsuariaYAlerta() {
        Alert alerta = new Alert();
        alerta.setId(5L);
        alerta.setUserProfileId(10L);
        alerta.setStatus("active");
        UserProfile perfil = new UserProfile();
        perfil.setId(10L);
        perfil.setUserId(1L);
        User u = new User();
        u.setId(1L);
        u.setFirstName("Ana");
        u.setLastName("Gómez");
        u.setEmail("ana@correo.com");
        when(evidenceRepository.findAll()).thenReturn(List.of(evidencia(1L, "pending", 100)));
        when(alertRepository.findAll()).thenReturn(List.of(alerta));
        when(userProfileRepository.findAll()).thenReturn(List.of(perfil));
        when(userRepository.findAll()).thenReturn(List.of(u));

        AdminEvidenceService.AdminEvidenceView vista = service.listar().get(0);

        assertEquals("Ana Gómez", vista.userName());
        assertEquals("active", vista.alertStatus());
        assertTrue(vista.hasFile());
    }

    @Test
    void estadisticas_cuentaPorRevisionYSumaTamanios() {
        when(evidenceRepository.findAll()).thenReturn(List.of(
                evidencia(1L, "verified", 100), evidencia(2L, "rejected", 50), evidencia(3L, "pending", 25)));

        AdminEvidenceService.EvidenceStats stats = service.estadisticas();

        assertEquals(3, stats.total());
        assertEquals(1, stats.verified());
        assertEquals(1, stats.pending());
        assertEquals(1, stats.rejected());
        assertEquals(175, stats.totalSizeBytes());
    }

    @Test
    void cambiarRevision_guardaElEstado() {
        Evidence e = evidencia(1L, "pending", 100);
        when(evidenceRepository.findById(1L)).thenReturn(Optional.of(e));
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(alertRepository.findById(5L)).thenReturn(Optional.empty());

        assertEquals("verified", service.cambiarRevision(1L, "verified").reviewStatus());
    }

    @Test
    void cambiarRevision_rechazaValoresDesconocidos() {
        assertThrows(IllegalArgumentException.class, () -> service.cambiarRevision(1L, "Verificada"));
    }
}

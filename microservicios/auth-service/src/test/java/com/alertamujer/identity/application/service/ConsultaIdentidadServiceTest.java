package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.out.CuentaRepositorio;
import com.alertamujer.identity.application.port.out.PerfilRepositorio;
import com.alertamujer.identity.application.port.out.UsuariaRepositorio;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaIdentidadServiceTest {

    @Mock private UsuariaRepositorio usuarias;
    @Mock private PerfilRepositorio perfiles;
    @Mock private CuentaRepositorio cuentas;

    @InjectMocks private ConsultaIdentidadService service;

    private static User usuaria(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    void porPerfilYPorCuentaPasanPorSuUsuaria() {
        UserProfile perfil = UserProfile.de(1L);
        Account cuenta = Account.nueva(1L, "hash");
        when(perfiles.buscar(10L)).thenReturn(Optional.of(perfil));
        when(cuentas.buscar(4L)).thenReturn(Optional.of(cuenta));
        when(usuarias.buscar(1L)).thenReturn(Optional.of(usuaria(1L)));

        assertEquals(1L, service.porPerfil(10L).orElseThrow().getId());
        assertEquals(1L, service.porCuenta(4L).orElseThrow().getId());
        assertTrue(service.porPerfil(null).isEmpty());
        assertTrue(service.usuaria(null).isEmpty());
        assertTrue(service.porCorreo(null).isEmpty());
    }

    @Test
    void porIdDePerfilOmiteLosPerfilesSinPersona() {
        UserProfile conPersona = UserProfile.de(1L);
        conPersona.setId(10L);
        UserProfile huerfano = UserProfile.de(99L);
        huerfano.setId(11L);
        when(usuarias.todas()).thenReturn(List.of(usuaria(1L)));
        when(perfiles.todos()).thenReturn(List.of(conPersona, huerfano));

        Map<Long, User> porPerfil = service.porIdDePerfil();

        assertEquals(1, porPerfil.size());
        assertEquals(1L, porPerfil.get(10L).getId());
    }
}

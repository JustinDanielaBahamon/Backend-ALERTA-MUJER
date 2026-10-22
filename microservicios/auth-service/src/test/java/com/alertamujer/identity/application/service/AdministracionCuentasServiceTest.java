package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.in.AdministrarCuentasUseCase.CuentaDeUsuaria;
import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.CambiosCuenta;
import com.alertamujer.identity.domain.model.DeviceSession;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministracionCuentasServiceTest {

    @Mock private UsuariaRepositorio usuarias;
    @Mock private CuentaRepositorio cuentas;
    @Mock private PerfilRepositorio perfiles;
    @Mock private SesionRepositorio sesiones;
    @Mock private CifradoPort cifrado;
    @Mock private EventosIdentidadPort events;

    @InjectMocks private AdministracionCuentasService service;

    private User ana;
    private Account cuenta;

    @BeforeEach
    void setUp() {
        ana = User.nueva("Ana Gómez", "ana@correo.com", "3001234567", "CC", "123", null);
        ana.setId(1L);
        cuenta = Account.nueva(1L, "hash");
        cuenta.setId(4L);
        lenient().when(usuarias.find(1L)).thenReturn(Optional.of(ana));
        lenient().when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
    }

    private static CambiosCuenta estado(String estado) {
        return new CambiosCuenta(estado, null, null, null, null);
    }

    @Test
    void cambiaElEstadoDeLaCuentaYLosDatos() {
        CuentaDeUsuaria r = service.actualizar(1L, new CambiosCuenta("blocked", "Ana María", null, null, null), 2L);

        assertEquals("blocked", r.cuenta().getStatus());
        assertEquals("Ana María", r.usuaria().getFirstName());
        assertEquals("Gómez", r.usuaria().getLastName());
        verify(cuentas).save(cuenta);
        verify(usuarias).save(ana);
    }

    @Test
    void noPuedeBloquearseASiMismaNiUsarEstadosDesconocidos() {
        IllegalArgumentException propia = assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, estado("blocked"), 1L));
        assertEquals("Cannot block your own account", propia.getMessage());
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, estado("raro"), 2L));
        verify(cuentas, never()).save(any());
    }

    @Test
    void noDejaUnCorreoRepetido() {
        User otra = new User();
        otra.setId(5L);
        when(usuarias.porCorreo("eva@correo.com")).thenReturn(Optional.of(otra));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, new CambiosCuenta(null, null, null, null, "eva@correo.com"), 2L));
        assertEquals("Email already in use", e.getMessage());
        verify(usuarias, never()).save(any());
    }

    @Test
    void anonimizarBorraLosDatosPersonalesYCierraLasSesiones() {
        UserProfile perfil = UserProfile.de(1L);
        perfil.setId(10L);
        perfil.setProfilePhotoUrl("data:image/jpeg;base64,/9j/4AAQ");
        when(perfiles.deUsuaria(1L)).thenReturn(Optional.of(perfil));
        DeviceSession sesion = new DeviceSession();
        when(sesiones.activasDeCuenta(4L)).thenReturn(List.of(sesion));
        when(cifrado.cifrar(anyString())).thenReturn("inutilizable");

        service.anonimizar(1L);

        assertEquals("eliminada-1@alertamujer.invalid", ana.getEmail());
        assertNull(perfil.getProfilePhotoUrl());
        assertEquals("inactive", cuenta.getStatus());
        assertEquals("inutilizable", cuenta.getPasswordHash());
        assertEquals("revoked", sesion.getStatus());
        verify(sesiones).save(sesion);
        // Alert y Evidence borran contactos, ubicaciones y grabaciones del perfil
        verify(events).cuentaEliminada(1L, 10L);
    }

    @Test
    void listarUneCadaPersonaConSuCuentaYSuPerfil() {
        UserProfile perfil = UserProfile.de(1L);
        perfil.setId(10L);
        when(usuarias.todas()).thenReturn(List.of(ana));
        when(cuentas.todas()).thenReturn(List.of(cuenta));
        when(perfiles.todos()).thenReturn(List.of(perfil));

        CuentaDeUsuaria r = service.listAll().get(0);

        assertSame(cuenta, r.cuenta());
        assertEquals(10L, r.perfilId());
    }
}

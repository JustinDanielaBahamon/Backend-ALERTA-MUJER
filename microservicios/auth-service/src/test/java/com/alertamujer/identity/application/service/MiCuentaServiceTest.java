package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.AdministrarCuentasUseCase;
import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.CambiosUsuaria;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiCuentaServiceTest {

    @Mock private UsuariaRepositorio usuarias;
    @Mock private PerfilRepositorio perfiles;
    @Mock private CuentaRepositorio cuentas;
    @Mock private CifradoPort cifrado;
    @Mock private EventosIdentidadPort events;
    @Mock private AdministrarCuentasUseCase administracion;

    @InjectMocks private MiCuentaService service;

    private User ana;
    private UserProfile perfil;
    private Account cuenta;

    @BeforeEach
    void setUp() {
        ana = User.nueva("Ana Gómez", "ana@correo.com", null, null, null, null);
        ana.setId(1L);
        perfil = UserProfile.de(1L);
        cuenta = Account.nueva(1L, "hash");
        cuenta.setId(4L);
        lenient().when(usuarias.find(1L)).thenReturn(Optional.of(ana));
        lenient().when(perfiles.deUsuaria(1L)).thenReturn(Optional.of(perfil));
        lenient().when(usuarias.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static CambiosUsuaria foto(Optional<String> foto) {
        return new CambiosUsuaria(null, null, null, null, null, null, foto);
    }

    @Test
    void completarDatos_rechazaTelefonoODocumentoDeOtraCuenta() {
        when(usuarias.existeTelefono("3001234567")).thenReturn(true);
        when(usuarias.existeDocumento("1080")).thenReturn(true);
        CambiosUsuaria cambios = new CambiosUsuaria(null, null, Optional.of("300 123 4567"), Optional.of("1080"),
                Optional.of("C.C"), null, null);

        DuplicateFieldsException e = assertThrows(DuplicateFieldsException.class, () -> service.actualizar(1L, cambios));
        assertEquals(java.util.Set.of("telephone", "documentNumber"), e.getFieldErrors().keySet());
        verify(usuarias, never()).save(any());
    }

    @Test
    void completarDatos_guardaElTelefonoLimpio() {
        CambiosUsuaria cambios = new CambiosUsuaria(null, null, Optional.of("300 123-4567"), Optional.of(" 1080 "),
                Optional.of("C.C"), java.time.LocalDate.of(1995, 5, 5), null);

        User saved = service.actualizar(1L, cambios).usuaria();

        assertEquals("3001234567", saved.getTelephone());
        assertEquals("1080", saved.getDocumentNumber());
    }

    @Test
    void guardaYQuitaLaFotoDePerfil() {
        assertEquals("data:image/jpeg;base64,/9j/4AAQ",
                service.actualizar(1L, foto(Optional.of("data:image/jpeg;base64,/9j/4AAQ"))).foto());
        assertNull(service.actualizar(1L, foto(Optional.of(""))).foto());
    }

    @Test
    void unaFotoQueNoEsImagenNoGuardaNada() {
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, foto(Optional.of("<script>"))));
        verify(usuarias, never()).save(any());
        verify(perfiles, never()).save(any());
    }

    @Test
    void cambiarContrasena_exigeLaActualYQueLaNuevaSeaDistinta() {
        when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
        when(cifrado.coincide("mala", "hash")).thenReturn(false);
        when(cifrado.coincide("Actual123", "hash")).thenReturn(true);
        when(cifrado.coincide("Nueva1234", "hash")).thenReturn(false);
        when(cifrado.cifrar("Nueva1234")).thenReturn("hash2");

        assertThrows(IllegalArgumentException.class, () -> service.cambiarContrasena(1L, "mala", "Nueva1234"));
        assertThrows(IllegalArgumentException.class, () -> service.cambiarContrasena(1L, "Actual123", "corta"));
        assertThrows(IllegalArgumentException.class, () -> service.cambiarContrasena(1L, "Actual123", "Actual123"));

        service.cambiarContrasena(1L, "Actual123", "Nueva1234");
        assertEquals("hash2", cuenta.getPasswordHash());
        verify(cuentas).save(cuenta);
        verifyNoInteractions(events);
    }

    @Test
    void elCambioDeContrasenaDeUnaAdministradoraQuedaEnLaAuditoria() {
        ana.setRoleId(User.ROL_ADMIN);
        when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
        when(cifrado.coincide("Actual123", "hash")).thenReturn(true);
        when(cifrado.coincide("Nueva1234", "hash")).thenReturn(false);
        when(cifrado.cifrar("Nueva1234")).thenReturn("hash2");

        service.cambiarContrasena(1L, "Actual123", "Nueva1234");

        verify(events).sesionAdmin(4L, AdminSessionEvent.PASSWORD_CHANGED, null);
    }

    @Test
    void eliminarMiCuenta_conLaPalabraDeConfirmacion_usaElMismoBorradoDeLaAdministracion() {
        service.eliminarMiCuenta(1L, " eliminar ");

        verify(administracion).anonimizar(1L);
    }

    @Test
    void eliminarMiCuenta_sinConfirmacionNoBorraNada() {
        assertThrows(IllegalArgumentException.class, () -> service.eliminarMiCuenta(1L, "si"));
        assertThrows(IllegalArgumentException.class, () -> service.eliminarMiCuenta(1L, null));
        verifyNoInteractions(administracion);
    }

    @Test
    void eliminarMiCuenta_unaAdministradoraNoPuedeHacerloSola() {
        ana.setRoleId(User.ROL_ADMIN);

        assertThrows(IllegalArgumentException.class, () -> service.eliminarMiCuenta(1L, "ELIMINAR"));
        verifyNoInteractions(administracion);
    }
}

package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.application.port.in.DatosRegistro;
import com.alertamujer.identity.application.port.in.Sesion;
import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.application.port.out.VerificadorGooglePort.IdentidadGoogle;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import com.alertamujer.shared.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Registro y sesión con los puertos simulados (sin base de datos, BCrypt ni JWT reales). */
@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

    @Mock private UsuariaRepositorio usuarias;
    @Mock private CuentaRepositorio cuentas;
    @Mock private PerfilRepositorio perfiles;
    @Mock private CifradoPort cifrado;
    @Mock private TokensPort tokens;
    @Mock private VerificadorGooglePort google;
    @Mock private EventosIdentidadPort eventos;

    @InjectMocks private SesionService service;

    private User ana;
    private Account cuenta;

    @BeforeEach
    void setUp() {
        ana = User.nueva("Ana Gómez", "ana@correo.com", null, null, null, null);
        ana.setId(1L);
        cuenta = Account.nueva(1L, "hash");
        cuenta.setId(4L);
        lenient().when(tokens.acceso(anyLong(), anyString())).thenReturn("acceso");
        lenient().when(tokens.renovacion(anyLong())).thenReturn("renovacion");
    }

    private static DatosRegistro registro() {
        return registro(true);
    }

    private static DatosRegistro registro(Boolean aceptaTerminos) {
        return new DatosRegistro("Ana Gómez", "ana@correo.com", "Secreta123", "300 123-4567", "CC", "123", "05/05/1995",
                aceptaTerminos);
    }

    @Test
    void registrar_creaPersonaCuentaYPerfilYAbreLaSesion() {
        when(usuarias.guardar(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(cifrado.cifrar("Secreta123")).thenReturn("hash");

        Sesion s = service.registrar(registro());

        assertEquals("acceso", s.token());
        assertEquals("renovacion", s.refreshToken());
        assertEquals("3001234567", s.usuaria().getTelephone());
        assertEquals(LocalDate.of(1995, 5, 5), s.usuaria().getBirthdate());
        ArgumentCaptor<Account> c = ArgumentCaptor.forClass(Account.class);
        verify(cuentas).guardar(c.capture());
        assertEquals("hash", c.getValue().getPasswordHash());
        assertEquals(1L, c.getValue().getUserId());
        verify(perfiles).guardar(any(UserProfile.class));
        verify(usuarias).asegurarRolUsuaria();
        assertNotNull(s.usuaria().getTermsAcceptedAt());
        assertEquals(User.TERMINOS_VIGENTES, s.usuaria().getTermsVersion());
    }

    @Test
    void registrar_rechazaSiNoAceptaLosTerminos() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(registro(false)));
        verify(usuarias, never()).guardar(any());
    }

    @Test
    void registrar_sinLaCasilla_noInventaLaConstancia() {
        when(usuarias.guardar(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(cifrado.cifrar("Secreta123")).thenReturn("hash");

        Sesion s = service.registrar(registro(null));

        assertNull(s.usuaria().getTermsAcceptedAt());
        assertNull(s.usuaria().getTermsVersion());
    }

    @Test
    void registrar_informaTodosLosCamposRepetidosALaVez() {
        when(usuarias.existeCorreo("ana@correo.com")).thenReturn(true);
        when(usuarias.existeTelefono("3001234567")).thenReturn(true);
        when(usuarias.existeDocumento("123")).thenReturn(true);

        DuplicateFieldsException e = assertThrows(DuplicateFieldsException.class, () -> service.registrar(registro()));

        assertEquals(3, e.getFieldErrors().size());
        verify(usuarias, never()).guardar(any());
    }

    @Test
    void conCorreo_entraYRegistraElAcceso() {
        when(usuarias.porCorreo("ana@correo.com")).thenReturn(Optional.of(ana));
        when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
        when(cifrado.coincide("Secreta123", "hash")).thenReturn(true);

        Sesion s = service.conCorreo("ana@correo.com", "Secreta123");

        assertSame(ana, s.usuaria());
        assertNotNull(cuenta.getLastAccess());
        verify(tokens).acceso(1L, "user");
        verifyNoInteractions(eventos);
    }

    @Test
    void conCorreo_rechazaCorreoDesconocidoContrasenaMalaOCuentaBloqueada() {
        when(usuarias.porCorreo("nadie@correo.com")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> service.conCorreo("nadie@correo.com", "x"));

        when(usuarias.porCorreo("ana@correo.com")).thenReturn(Optional.of(ana));
        when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
        when(cifrado.coincide("mala", "hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> service.conCorreo("ana@correo.com", "mala"));

        cuenta.cambiarEstado("blocked");
        InvalidCredentialsException e = assertThrows(InvalidCredentialsException.class,
                () -> service.conCorreo("ana@correo.com", "Secreta123"));
        assertEquals("Account blocked", e.getMessage());
        verify(cuentas, never()).guardar(any());
    }

    @Test
    void conCorreo_unaAdministradoraQuedaEnLaAuditoria() {
        ana.setRoleId(User.ROL_ADMIN);
        when(usuarias.porCorreo("ana@correo.com")).thenReturn(Optional.of(ana));
        when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
        when(cifrado.coincide("mala", "hash")).thenReturn(false);
        when(cifrado.coincide("Secreta123", "hash")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> service.conCorreo("ana@correo.com", "mala"));
        service.conCorreo("ana@correo.com", "Secreta123");

        verify(eventos).sesionAdmin(4L, AdminSessionEvent.LOGIN_FAILED, "Contraseña incorrecta");
        verify(eventos).sesionAdmin(4L, AdminSessionEvent.LOGIN_SUCCESS, "Con correo y contraseña");
        verify(tokens).acceso(1L, "admin");
    }

    @Test
    void conGoogle_creaLaUsuariaSiNoExiste() {
        when(google.verificar("token")).thenReturn(new IdentidadGoogle("eva@correo.com", "Eva Ruiz", "Eva", "Ruiz"));
        when(usuarias.porCorreo("eva@correo.com")).thenReturn(Optional.empty());
        when(usuarias.guardar(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(9L);
            return u;
        });
        when(cifrado.cifrar(anyString())).thenReturn("aleatoria");
        Account nueva = Account.nueva(9L, "aleatoria");
        when(cuentas.deUsuaria(9L)).thenReturn(Optional.of(nueva));

        Sesion s = service.conGoogle("token");

        assertEquals("Eva", s.usuaria().getFirstName());
        assertEquals(User.TERMINOS_VIGENTES, s.usuaria().getTermsVersion());
        verify(perfiles).guardar(any(UserProfile.class));
        verify(cuentas, times(2)).guardar(any(Account.class));
    }

    @Test
    void renovar_entregaUnTokenNuevoSoloSiLaCuentaSigueActiva() {
        when(tokens.usuariaDeRenovacion("r")).thenReturn(1L);
        when(usuarias.buscar(1L)).thenReturn(Optional.of(ana));
        when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));

        assertEquals("acceso", service.renovar("r"));

        cuenta.cambiarEstado("blocked");
        InvalidCredentialsException e = assertThrows(InvalidCredentialsException.class, () -> service.renovar("r"));
        assertEquals("Refresh token inválido o expirado", e.getMessage());
    }

    @Test
    void renovar_unTokenInvalidoDaElMismoMensaje() {
        when(tokens.usuariaDeRenovacion("basura")).thenThrow(new IllegalArgumentException("firma"));

        InvalidCredentialsException e = assertThrows(InvalidCredentialsException.class, () -> service.renovar("basura"));
        assertEquals("Refresh token inválido o expirado", e.getMessage());
    }

    @Test
    void cerrar_soloAuditaAdministradoras() {
        when(usuarias.buscar(1L)).thenReturn(Optional.of(ana));
        service.cerrar(1L, "inactividad");
        service.cerrar(null, null);
        verifyNoInteractions(eventos);

        ana.setRoleId(User.ROL_ADMIN);
        when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
        service.cerrar(1L, "inactividad");
        verify(eventos).sesionAdmin(4L, AdminSessionEvent.LOGOUT, "Por inactividad");
    }
}

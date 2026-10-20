package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.out.*;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import com.alertamujer.identity.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacionContrasenaServiceTest {

    @Mock private UsuariaRepositorio usuarias;
    @Mock private CuentaRepositorio cuentas;
    @Mock private RecuperacionRepositorio solicitudes;
    @Mock private CifradoPort cifrado;
    @Mock private EventosIdentidadPort eventos;

    @InjectMocks private RecuperacionContrasenaService service;

    private User ana;
    private Account cuenta;
    private final List<RecoveryRequest> pendientes = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ana = User.nueva("Ana Gómez", "ana@correo.com", null, null, null, null);
        ana.setId(1L);
        cuenta = Account.nueva(1L, "hash");
        cuenta.setId(4L);
        lenient().when(usuarias.porCorreo("ana@correo.com")).thenReturn(Optional.of(ana));
        lenient().when(cuentas.deUsuaria(1L)).thenReturn(Optional.of(cuenta));
        lenient().when(cuentas.buscar(4L)).thenReturn(Optional.of(cuenta));
        lenient().when(solicitudes.pendientesDeCuenta(4L)).thenAnswer(inv -> List.copyOf(pendientes));
    }

    private RecoveryRequest pendiente(String codigo, LocalDateTime creada) {
        RecoveryRequest r = RecoveryRequest.nueva(4L, codigo, null, creada);
        pendientes.add(r);
        return r;
    }

    @Test
    void enviaUnCodigoDeSeisDigitosYGuardaSoloSuHash() {
        service.solicitar(" ana@correo.com ", "1.2.3.4");

        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(eventos).codigoRecuperacion(eq("ana@correo.com"), eq("Ana Gómez"), codigo.capture(), eq(15));
        assertTrue(codigo.getValue().matches("\\d{6}"));
        ArgumentCaptor<RecoveryRequest> guardada = ArgumentCaptor.forClass(RecoveryRequest.class);
        verify(solicitudes).guardar(guardada.capture());
        assertEquals(RecoveryRequest.hash(4L, codigo.getValue()), guardada.getValue().getTokenHash());
        assertEquals("1.2.3.4", guardada.getValue().getRequestedIp());
    }

    @Test
    void correoNoRegistradoOCuentaBloqueadaNoRevelaNada() {
        when(usuarias.porCorreo("nadie@correo.com")).thenReturn(Optional.empty());
        service.solicitar("nadie@correo.com", null);

        cuenta.cambiarEstado("blocked");
        service.solicitar("ana@correo.com", null);

        verifyNoInteractions(eventos);
        verify(solicitudes, never()).guardar(any());
    }

    @Test
    void noEnviaOtroCodigoAntesDeUnMinutoYElNuevoReemplazaAlAnterior() {
        RecoveryRequest reciente = pendiente("111111", LocalDateTime.now().minusSeconds(10));
        service.solicitar("ana@correo.com", null);
        verifyNoInteractions(eventos);

        reciente.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        service.solicitar("ana@correo.com", null);
        assertEquals("replaced", reciente.getStatus());
        verify(eventos).codigoRecuperacion(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void conElCodigoCorrectoCambiaLaContrasenaYLoDaPorUsado() {
        RecoveryRequest r = pendiente("123456", LocalDateTime.now());
        when(cifrado.cifrar("NuevaClave1")).thenReturn("hash2");

        service.restablecer("ana@correo.com", "123456", "NuevaClave1");

        assertEquals("hash2", cuenta.getPasswordHash());
        assertEquals("used", r.getStatus());
        assertNotNull(r.getUsedAt());
    }

    @Test
    void cincoIntentosFallidosAnulanElCodigo() {
        RecoveryRequest r = pendiente("123456", LocalDateTime.now());

        for (int i = 0; i < RecoveryRequest.MAX_INTENTOS; i++) {
            assertThrows(IllegalArgumentException.class, () -> service.verificar("ana@correo.com", "000000"));
        }

        assertEquals("expired", r.getStatus());
        assertEquals(5L, r.getAttempts());
    }

    @Test
    void codigoVencidoOConFormatoInvalidoNoSirve() {
        RecoveryRequest r = pendiente("123456", LocalDateTime.now().minusMinutes(20));

        assertThrows(IllegalArgumentException.class, () -> service.verificar("ana@correo.com", "12ab56"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.verificar("ana@correo.com", "123456"));

        assertEquals(RecuperacionContrasenaService.CODIGO_INVALIDO, e.getMessage());
        assertEquals("expired", r.getStatus());
    }
}

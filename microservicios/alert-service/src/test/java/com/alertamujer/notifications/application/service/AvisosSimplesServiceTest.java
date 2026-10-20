package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.out.CorreosPort;
import com.alertamujer.notifications.application.port.out.CorreosPort.CorreoNoEnviadoException;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Aviso de celular nuevo y código de recuperación: un fallo no debe romper a quien publicó el evento. */
class AvisosSimplesServiceTest {

    @Test
    void avisaALosOtrosCelularesDeLaUsuaria() {
        PushPort push = mock(PushPort.class);

        new AvisoNuevoCelularService(push).avisar(List.of("t1"), "Moto G");

        verify(push).enviar(List.of("t1"), Map.of("tipo", "NUEVO_CELULAR", "celular", "Moto G"));
    }

    @Test
    void unFalloDelPushODelCorreoSoloQuedaEnElLog() {
        PushPort push = mock(PushPort.class);
        when(push.enviar(anyList(), anyMap())).thenThrow(new IllegalStateException("FCM caído"));
        CorreosPort correos = mock(CorreosPort.class);
        doThrow(new CorreoNoEnviadoException("Sin proveedor")).when(correos).codigoRecuperacion(any(), any(), any(), anyInt());

        assertDoesNotThrow(() -> new AvisoNuevoCelularService(push).avisar(List.of("t1"), "Moto G"));
        assertDoesNotThrow(() -> new CodigoRecuperacionService(correos).enviar("ana@correo.com", "Ana", "123456", 15));
    }
}

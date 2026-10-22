package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.out.EmailsPort;
import com.alertamujer.notifications.application.port.out.EmailsPort.EmailNotSentException;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Aviso de celular nuevo y código de recuperación: un fallo no debe romper a quien publicó el evento. */
class SimpleNoticesServiceTest {

    @Test
    void notifiesTheUsersOtherPhones() {
        PushPort push = mock(PushPort.class);

        new NewPhoneNoticeService(push).notify(List.of("t1"), "Moto G");

        verify(push).send(List.of("t1"), Map.of("tipo", "NUEVO_CELULAR", "celular", "Moto G"));
    }

    @Test
    void aPushOrEmailFailureOnlyGoesToTheLog() {
        PushPort push = mock(PushPort.class);
        when(push.send(anyList(), anyMap())).thenThrow(new IllegalStateException("FCM caído"));
        EmailsPort emails = mock(EmailsPort.class);
        doThrow(new EmailNotSentException("Sin proveedor")).when(emails).recoveryCode(any(), any(), any(), anyInt());

        assertDoesNotThrow(() -> new NewPhoneNoticeService(push).notify(List.of("t1"), "Moto G"));
        assertDoesNotThrow(() -> new RecoveryCodeService(emails).send("ana@correo.com", "Ana", "123456", 15));
    }
}

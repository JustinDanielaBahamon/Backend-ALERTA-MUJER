package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevicesApiServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private IdentityApi identityApi;

    @InjectMocks
    private DevicesApiService api;

    private Device dispositivo(String token) {
        Device d = new Device();
        d.setFcmToken(token);
        return d;
    }

    @Test
    void pushTokensOfProfile_buscaPorLaCuentaDeLaDuenaDelPerfil() {
        // perfil 10 -> usuaria 7 -> cuenta 39 (ids distintos, como en producción)
        when(identityApi.findUserByProfileId(10L)).thenReturn(Optional.of(new UserSummary(7L, "Ana", null, "a@t.co", null, 1L, null)));
        when(identityApi.requireAccountId(7L)).thenReturn(39L);
        when(deviceRepository.findByAccountId(39L)).thenReturn(List.of(dispositivo("tok-1"), dispositivo(" "), dispositivo(null)));

        assertEquals(List.of("tok-1"), api.pushTokensOfProfile(10L));
    }

    @Test
    void pushTokensOfProfile_sinUsuariaDevuelveVacio() {
        when(identityApi.findUserByProfileId(99L)).thenReturn(Optional.empty());

        assertEquals(List.of(), api.pushTokensOfProfile(99L));
    }

    @Test
    void pushTokensOfUser_sinCuentaDevuelveVacio() {
        when(identityApi.requireAccountId(5L)).thenThrow(new com.alertamujer.shared.exception.ResourceNotFoundException("Account for user", 5L));

        assertEquals(List.of(), api.pushTokensOfUser(5L));
    }

    @Test
    void pushTokensOfUser_usaLaCuentaDeLaPersona() {
        when(identityApi.requireAccountId(7L)).thenReturn(39L);
        when(deviceRepository.findByAccountId(39L)).thenReturn(List.of(dispositivo("tok-2")));

        assertEquals(List.of("tok-2"), api.pushTokensOfUser(7L));
    }

    @Test
    void phonePushTokensOfUser_dejaFueraLosNavegadores() {
        when(identityApi.requireAccountId(7L)).thenReturn(39L);
        var celular = dispositivo("tok-celular");
        celular.setOsName("android");
        var navegador = dispositivo("tok-navegador");
        navegador.setOsName("web");
        when(deviceRepository.findByAccountId(39L)).thenReturn(List.of(celular, navegador));

        assertEquals(List.of("tok-celular", "tok-navegador"), api.pushTokensOfUser(7L));
        assertEquals(List.of("tok-celular"), api.phonePushTokensOfUser(7L));
    }
}

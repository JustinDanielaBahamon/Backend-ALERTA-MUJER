package com.alertamujer.devices.application.service;

import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.devices.infrastructure.repository.DeviceRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminDeviceService service;

    private Device dispositivo() {
        Device d = new Device();
        d.setId(7L);
        d.setAccountId(3L);
        d.setBrand("Infinix");
        d.setModel("X678B");
        d.setStatus("active");
        d.setFcmToken("token-secreto");
        return d;
    }

    @Test
    void listar_agregaLaDueñaSinExponerElToken() {
        Account cuenta = new Account();
        cuenta.setId(3L);
        cuenta.setUserId(1L);
        User u = new User();
        u.setId(1L);
        u.setFirstName("Ana");
        u.setLastName("Gómez");
        u.setEmail("ana@correo.com");
        when(deviceRepository.findAll()).thenReturn(List.of(dispositivo()));
        when(accountRepository.findAll()).thenReturn(List.of(cuenta));
        when(userRepository.findAll()).thenReturn(List.of(u));

        AdminDeviceService.AdminDeviceView vista = service.listar().get(0);

        assertEquals("Ana Gómez", vista.ownerName());
        assertEquals("ana@correo.com", vista.ownerEmail());
        assertTrue(vista.pushTokenRegistered());
    }

    @Test
    void cambiarEstado_guardaElNuevoEstado() {
        Device d = dispositivo();
        when(deviceRepository.findById(7L)).thenReturn(Optional.of(d));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.findById(3L)).thenReturn(Optional.empty());

        AdminDeviceService.AdminDeviceView vista = service.cambiarEstado(7L, "blocked");

        assertEquals("blocked", vista.status());
        assertFalse(vista.ownerName() != null);
        verify(deviceRepository).save(d);
    }

    @Test
    void cambiarEstado_rechazaEstadosDesconocidos() {
        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(7L, "Bloqueado"));
    }
}

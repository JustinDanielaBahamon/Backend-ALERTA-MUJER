package com.alertamujer.devices.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas del dispositivo, sin Spring ni base de datos. */
class DeviceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static DatosDispositivo data(String marca, String name, String gps, String permisos) {
        return new DatosDispositivo("uuid", marca, null, name, null, null, null, gps, null, null, null, permisos);
    }

    private static Device phone(Long id, String os, LocalDateTime acceso) {
        Device d = new Device();
        d.setId(id);
        d.setOsName(os);
        d.setLastAccess(acceso);
        return d;
    }

    @Test
    void unoNuevoQuedaActivoConGpsDesconocidoYConectadoAhora() {
        Device d = Device.nuevo(4L, data("Samsung", null, null, null), AHORA);

        assertEquals(4L, d.getAccountId());
        assertEquals("unknown", d.getGpsStatus());
        assertEquals("active", d.getStatus());
        assertEquals(AHORA, d.getLastAccess());
    }

    @Test
    void registrarUsoActualizaSoloLoQueLlegaRecortadoYMarcaLaConexion() {
        Device d = Device.nuevo(4L, data("Samsung", null, null, null), AHORA.minusDays(1));

        d.registrarUso(data(null, "  Galaxy de Ana  ", "active", "{ \"ubicacion\": true }"), AHORA);

        assertEquals("Samsung", d.getBrand());
        assertEquals("Galaxy de Ana", d.getDeviceName());
        assertEquals("active", d.getGpsStatus());
        assertEquals("{\"ubicacion\":true}", d.getPermissions());
        assertEquals(AHORA, d.getPermissionsUpdatedAt());
        assertEquals(AHORA, d.getLastAccess());
    }

    @Test
    void soloAceptaPermisosDeSiONo() {
        assertEquals("{}", Device.validarPermisos("{}"));
        assertThrows(IllegalArgumentException.class, () -> Device.validarPermisos("{\"x\":\"<script>\"}"));
        assertThrows(IllegalArgumentException.class, () -> Device.validarPermisos("[true]"));
    }

    @Test
    void reemplazarConservaElTokenSiNoLlegaUnoNuevo() {
        Device d = new Device();
        d.setFcmToken("token");

        d.reemplazar(new DatosDispositivo("uuid", "Xiaomi", "Note", null, "android", "14", "1.0", "active", "active", null, "", null));

        assertEquals("Xiaomi", d.getBrand());
        assertEquals("token", d.getFcmToken());
    }

    @Test
    void elNombreDelAvisoUsaElNombreOMarcaYModelo() {
        Device d = new Device();
        assertEquals("otro celular", d.nombreParaAviso());
        d.setBrand("Samsung");
        d.setModel("A54");
        assertEquals("Samsung A54", d.nombreParaAviso());
        d.setDeviceName("Galaxy de Ana");
        assertEquals("Galaxy de Ana", d.nombreParaAviso());
    }

    @Test
    void laUbicacionDiariaSoloSeTomaDelCelularMasReciente() {
        List<Device> cuenta = List.of(phone(1L, "android", AHORA.minusHours(2)), phone(2L, "android", AHORA),
                phone(3L, "web", AHORA.plusHours(1)));

        assertTrue(Device.esElCelularMasReciente(cuenta, 2L));
        assertFalse(Device.esElCelularMasReciente(cuenta, 1L));
        assertTrue(Device.esElCelularMasReciente(cuenta, null));
        assertTrue(Device.esElCelularMasReciente(cuenta, 99L));
    }

    @Test
    void laAdministracionSoloAsignaEstadosConocidos() {
        Device d = new Device();
        d.cambiarEstado("lost");
        assertEquals("lost", d.getStatus());
        assertThrows(IllegalArgumentException.class, () -> d.cambiarEstado("roto"));
    }
}

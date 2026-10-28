package com.alertamujer.devices.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Device rules, without Spring or a database. */
class DeviceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    private static DeviceData data(String brand, String name, String gps, String permissions) {
        return new DeviceData("uuid", brand, null, name, null, null, null, gps, null, null, null, permissions);
    }

    private static Device phone(Long id, String os, LocalDateTime access) {
        Device d = new Device();
        d.setId(id);
        d.setOsName(os);
        d.setLastAccess(access);
        return d;
    }

    @Test
    void aNewOneIsActiveWithUnknownGpsAndConnectedNow() {
        Device d = Device.created(4L, data("Samsung", null, null, null), NOW);

        assertEquals(4L, d.getAccountId());
        assertEquals("unknown", d.getGpsStatus());
        assertEquals("active", d.getStatus());
        assertEquals(NOW, d.getLastAccess());
    }

    @Test
    void recordUseUpdatesOnlyWhatArrivesTruncatedAndMarksTheConnection() {
        Device d = Device.created(4L, data("Samsung", null, null, null), NOW.minusDays(1));

        d.recordUse(data(null, "  Galaxy de Ana  ", "active", "{ \"ubicacion\": true }"), NOW);

        assertEquals("Samsung", d.getBrand());
        assertEquals("Galaxy de Ana", d.getDeviceName());
        assertEquals("active", d.getGpsStatus());
        assertEquals("{\"ubicacion\":true}", d.getPermissions());
        assertEquals(NOW, d.getPermissionsUpdatedAt());
        assertEquals(NOW, d.getLastAccess());
    }

    @Test
    void onlyAcceptsYesOrNoPermissions() {
        assertEquals("{}", Device.validatePermissions("{}"));
        assertThrows(IllegalArgumentException.class, () -> Device.validatePermissions("{\"x\":\"<script>\"}"));
        assertThrows(IllegalArgumentException.class, () -> Device.validatePermissions("[true]"));
    }

    @Test
    void replaceKeepsTheTokenIfNoNewOneArrives() {
        Device d = new Device();
        d.setFcmToken("token");

        d.replace(new DeviceData("uuid", "Xiaomi", "Note", null, "android", "14", "1.0", "active", "active", null, "", null));

        assertEquals("Xiaomi", d.getBrand());
        assertEquals("token", d.getFcmToken());
    }

    @Test
    void theNoticeNameUsesTheNameOrBrandAndModel() {
        Device d = new Device();
        assertEquals("otro celular", d.nameForNotice());
        d.setBrand("Samsung");
        d.setModel("A54");
        assertEquals("Samsung A54", d.nameForNotice());
        d.setDeviceName("Galaxy de Ana");
        assertEquals("Galaxy de Ana", d.nameForNotice());
    }

    @Test
    void theDailyLocationIsTakenOnlyFromTheNewestPhone() {
        List<Device> account = List.of(phone(1L, "android", NOW.minusHours(2)), phone(2L, "android", NOW),
                phone(3L, "web", NOW.plusHours(1)));

        assertTrue(Device.isTheNewestPhone(account, 2L));
        assertFalse(Device.isTheNewestPhone(account, 1L));
        assertTrue(Device.isTheNewestPhone(account, null));
        assertTrue(Device.isTheNewestPhone(account, 99L));
    }

    @Test
    void theAdministrationOnlyAssignsKnownStatuses() {
        Device d = new Device();
        d.changeStatus("lost");
        assertEquals("lost", d.getStatus());
        assertThrows(IllegalArgumentException.class, () -> d.changeStatus("roto"));
    }
}

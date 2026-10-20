package com.alertamujer.devices.domain.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Dispositivo de una cuenta: celular con la app o navegador del panel web (osName = "web"). Guarda
 * el token push y los permisos que informa la app. Modelo del dominio: no conoce la base de datos ni Spring.
 */
public class Device {

    private Long id;
    private Long accountId;
    private String deviceUuid;
    private String brand;
    private String model;
    /** Nombre del celular en Android ("Galaxy de Ana"); lo envía la app. */
    private String deviceName;
    private String osName;
    private String osVersion;
    private String appVersion;
    /** active | inactive | unknown | denied */
    private String gpsStatus = "unknown";
    /** active | inactive | blocked | lost */
    private String status = "active";
    private LocalDateTime lastAccess;
    private LocalDateTime createdAt = LocalDateTime.now();
    /** Token de Firebase Cloud Messaging para las notificaciones push. */
    private String fcmToken;
    /** Permisos que informa la app como JSON de sí/no ({"ubicacion":true,...}). */
    private String permissions;
    private LocalDateTime permissionsUpdatedAt;

    /** osName con el que se registra el panel web (recibe notificaciones del navegador). */
    public static final String OS_WEB = "web";
    public static final Set<String> ESTADOS = Set.of("active", "inactive", "blocked", "lost");

    // Solo un objeto plano de nombres cortos con true/false: lo muestra la web, no se guarda otra cosa
    private static final Pattern PERMISOS =
            Pattern.compile("^\\{(\"[a-zA-Z]{1,30}\":(true|false)(,\"[a-zA-Z]{1,30}\":(true|false))*)?}$");
    private static final int MAXIMO_PERMISOS = 400;

    // ── Reglas ──────────────────────────────────────────────────────

    /** Dispositivo nuevo de la cuenta; la última conexión es ahora. */
    public static Device nuevo(Long accountId, DatosDispositivo d, LocalDateTime ahora) {
        Device n = new Device();
        n.accountId = accountId;
        n.deviceUuid = d.deviceUuid();
        n.brand = d.brand();
        n.model = d.model();
        n.deviceName = d.deviceName();
        n.osName = d.osName();
        n.osVersion = d.osVersion();
        n.appVersion = d.appVersion();
        if (d.gpsStatus() != null) n.gpsStatus = d.gpsStatus();
        if (d.status() != null) n.status = d.status();
        n.fcmToken = d.fcmToken();
        n.permissions = d.permissions();
        n.lastAccess = ahora;
        return n;
    }

    /** Solo acepta un JSON plano de nombres cortos con true/false; lo devuelve sin espacios. */
    public static String validarPermisos(String permisos) {
        String valor = permisos.replaceAll("\\s", "");
        if (valor.length() > MAXIMO_PERMISOS || !PERMISOS.matcher(valor).matches()) {
            throw new IllegalArgumentException("Permisos del celular no válidos");
        }
        return valor;
    }

    /**
     * true si el dispositivo es el celular de la cuenta usado más recientemente, o si no se sabe
     * (sin id o desconocido): así una app antigua no pierde sus ubicaciones.
     */
    public static boolean esElCelularMasReciente(List<Device> deLaCuenta, Long deviceId) {
        if (deviceId == null) return true;
        List<Device> celulares = deLaCuenta.stream().filter(Device::esCelular).toList();
        if (celulares.stream().noneMatch(d -> d.id.equals(deviceId))) return true;
        return celulares.stream()
                .max(Comparator.comparing(Device::getLastAccess, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(d -> d.id.equals(deviceId))
                .orElse(true);
    }

    public boolean esCelular() {
        return !OS_WEB.equalsIgnoreCase(osName);
    }

    public boolean esDe(Long cuenta) {
        return cuenta != null && Objects.equals(accountId, cuenta);
    }

    public boolean tieneTokenPush() {
        return fcmToken != null && !fcmToken.isBlank();
    }

    /** Cómo se nombra el celular en el aviso "entraron desde otro celular". */
    public String nombreParaAviso() {
        if (deviceName != null && !deviceName.isBlank()) return deviceName;
        String marcaModelo = ((brand == null ? "" : brand) + " " + (model == null ? "" : model)).trim();
        return marcaModelo.isEmpty() ? "otro celular" : marcaModelo;
    }

    /** PUT completo: reemplaza los datos; el token push solo si llega uno. */
    public void reemplazar(DatosDispositivo d) {
        deviceUuid = d.deviceUuid();
        brand = d.brand();
        model = d.model();
        osName = d.osName();
        osVersion = d.osVersion();
        appVersion = d.appVersion();
        gpsStatus = d.gpsStatus();
        status = d.status();
        lastAccess = d.lastAccess();
        if (d.fcmToken() != null && !d.fcmToken().isEmpty()) fcmToken = d.fcmToken();
    }

    /**
     * La app lo informa al abrirse: actualiza los datos que lleguen (sin borrar los que no envía),
     * los permisos y la última conexión.
     */
    public void registrarUso(DatosDispositivo d, LocalDateTime ahora) {
        if (d.brand() != null) brand = recortar(d.brand(), 50);
        if (d.model() != null) model = recortar(d.model(), 100);
        if (d.deviceName() != null) deviceName = recortar(d.deviceName(), 100);
        if (d.osName() != null) osName = recortar(d.osName(), 30);
        if (d.osVersion() != null) osVersion = recortar(d.osVersion(), 30);
        if (d.appVersion() != null) appVersion = recortar(d.appVersion(), 30);
        if (d.gpsStatus() != null) gpsStatus = d.gpsStatus();
        if (d.permissions() != null) {
            permissions = validarPermisos(d.permissions());
            permissionsUpdatedAt = ahora;
        }
        lastAccess = ahora;
    }

    public void cambiarTokenPush(String token) {
        fcmToken = token;
    }

    /** Cambio de estado desde la administración. */
    public void cambiarEstado(String estado) {
        if (estado == null || !ESTADOS.contains(estado)) throw new IllegalArgumentException("Estado no válido: " + estado);
        status = estado;
    }

    private static String recortar(String valor, int max) {
        String v = valor.trim();
        return v.length() > max ? v.substring(0, max) : v;
    }

    // ── Datos ───────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getDeviceUuid() { return deviceUuid; }
    public void setDeviceUuid(String deviceUuid) { this.deviceUuid = deviceUuid; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    public String getGpsStatus() { return gpsStatus; }
    public void setGpsStatus(String gpsStatus) { this.gpsStatus = gpsStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastAccess() { return lastAccess; }
    public void setLastAccess(LocalDateTime lastAccess) { this.lastAccess = lastAccess; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public LocalDateTime getPermissionsUpdatedAt() { return permissionsUpdatedAt; }
    public void setPermissionsUpdatedAt(LocalDateTime permissionsUpdatedAt) { this.permissionsUpdatedAt = permissionsUpdatedAt; }
}

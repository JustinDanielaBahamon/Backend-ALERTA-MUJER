package com.alertamujer.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Datos de la petición HTTP en curso que se guardan en la auditoría. */
public final class RequestInfo {

    private RequestInfo() {}

    /**
     * IP del cliente. Detrás del proxy de Render, Tomcat ya la resolvió desde X-Forwarded-For
     * (server.forward-headers-strategy=native) tomando la que agregó el proxy, no la que el cliente
     * pudiera inventar en la cabecera. Solo IPv4/IPv6 de hasta 45 caracteres.
     */
    public static String ip() {
        HttpServletRequest request = current();
        if (request == null) return null;
        String ip = request.getRemoteAddr();
        return ip != null && ip.matches("[0-9a-fA-F.:]{2,45}") ? ip : null;
    }

    /** Navegador y sistema en pocas palabras (p. ej. "Chrome · Windows"), a partir del User-Agent. */
    public static String device() {
        HttpServletRequest request = current();
        String ua = request == null ? null : request.getHeader("User-Agent");
        return describe(ua);
    }

    /**
     * Desde dónde llega la petición: "App móvil · Android" o "Panel web · Chrome · Linux".
     * La app (React Native) usa okhttp en Android; cualquier navegador se toma como el panel web.
     */
    public static String source() {
        HttpServletRequest request = current();
        if (request == null) return null;
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) return "Cliente sin identificar";
        if (ua.contains("okhttp") || ua.contains("Expo") || ua.contains("ReactNative")) {
            return ua.contains("iPhone") || ua.contains("Darwin") ? "App móvil · iOS" : "App móvil · Android";
        }
        if (ua.startsWith("Mozilla/")) return "Panel web · " + describe(ua);
        String shortName = ua.split("[ /]")[0];
        return "Cliente " + (shortName.length() > 40 ? shortName.substring(0, 40) : shortName);
    }

    static String describe(String ua) {
        if (ua == null || ua.isBlank()) return null;
        String browser = ua.contains("Edg/") ? "Edge"
                : ua.contains("OPR/") ? "Opera"
                : ua.contains("Firefox/") ? "Firefox"
                : ua.contains("Chrome/") ? "Chrome"
                : ua.contains("Safari/") ? "Safari"
                : ua.contains("okhttp") ? "App móvil"
                : "Otro";
        String system = ua.contains("Android") ? "Android"
                : ua.contains("iPhone") || ua.contains("iPad") ? "iOS"
                : ua.contains("Windows") ? "Windows"
                : ua.contains("Mac OS") ? "macOS"
                : ua.contains("Linux") ? "Linux"
                : null;
        return system == null ? browser : browser + " · " + system;
    }

    private static HttpServletRequest current() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs ? attrs.getRequest() : null;
    }
}

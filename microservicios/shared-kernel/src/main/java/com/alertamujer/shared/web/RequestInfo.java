package com.alertamujer.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Data of the current HTTP request that is stored in the audit. */
public final class RequestInfo {

    private RequestInfo() {}

    /**
     * Client IP. Behind the Render proxy, Tomcat already resolved it from X-Forwarded-For
     * (server.forward-headers-strategy=native) taking the one the proxy added, not one the client
     * could invent in the header. Only IPv4/IPv6 up to 45 characters.
     */
    public static String ip() {
        HttpServletRequest request = current();
        if (request == null) return null;
        String ip = request.getRemoteAddr();
        return ip != null && ip.matches("[0-9a-fA-F.:]{2,45}") ? ip : null;
    }

    /** Browser and system in a few words (e.g. "Chrome · Windows"), from the User-Agent. */
    public static String device() {
        HttpServletRequest request = current();
        String ua = request == null ? null : request.getHeader("User-Agent");
        return describe(ua);
    }

    /**
     * Where the request comes from: "App móvil · Android" or "Panel web · Chrome · Linux".
     * The app (React Native) uses okhttp on Android; any browser is taken as the web panel.
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

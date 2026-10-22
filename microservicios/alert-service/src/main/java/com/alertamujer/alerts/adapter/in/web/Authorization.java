package com.alertamujer.alerts.adapter.in.web;

import org.springframework.security.core.Authentication;

/** Controller helper: the role comes from the JWT. */
final class Authorization {

    private Authorization() {
    }

    static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}

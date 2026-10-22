package com.alertamujer.admin.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/** Puerto de salida hacia identity: nombres y correos para las vistas del panel. */
public interface UsersPort {

    Optional<UserSummary> find(Long userId);

    Optional<UserSummary> withEmail(String email);

    Map<Long, UserSummary> byId();

    Map<Long, UserSummary> byAccount();
}

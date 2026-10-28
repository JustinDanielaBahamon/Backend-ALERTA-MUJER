package com.alertamujer.admin.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/** Output port to identity: names and emails for the panel views. */
public interface UsersPort {

    Optional<UserSummary> find(Long userId);

    Optional<UserSummary> withEmail(String email);

    Map<Long, UserSummary> byId();

    Map<Long, UserSummary> byAccount();
}

package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.User;

import java.util.Map;
import java.util.Optional;

/** Consultas que piden los otros módulos y microservicios. */
public interface QueryIdentityUseCase {

    Optional<Long> profileOf(Long userId);

    Optional<Long> accountOf(Long userId);

    Optional<User> user(Long userId);

    Optional<User> byEmail(String email);

    Optional<User> byProfile(Long profileId);

    Optional<User> byAccount(Long accountId);

    Map<Long, User> byId();

    Map<Long, User> byProfileId();

    Map<Long, User> byAccountId();
}

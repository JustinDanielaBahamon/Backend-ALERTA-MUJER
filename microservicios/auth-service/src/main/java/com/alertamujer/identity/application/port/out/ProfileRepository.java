package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.UserProfile;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los perfiles de las usuarias. */
public interface ProfileRepository {

    UserProfile save(UserProfile profile);

    Optional<UserProfile> find(Long id);

    Optional<UserProfile> ofUser(Long userId);

    boolean existsForUser(Long userId);

    List<UserProfile> all();
}

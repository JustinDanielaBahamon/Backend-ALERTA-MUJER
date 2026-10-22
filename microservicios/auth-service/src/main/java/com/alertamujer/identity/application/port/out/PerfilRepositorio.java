package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.UserProfile;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los perfiles de las usuarias. */
public interface PerfilRepositorio {

    UserProfile save(UserProfile perfil);

    Optional<UserProfile> find(Long id);

    Optional<UserProfile> deUsuaria(Long userId);

    boolean existeDeUsuaria(Long userId);

    List<UserProfile> todos();
}

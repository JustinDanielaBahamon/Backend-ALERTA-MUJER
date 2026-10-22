package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.User;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las personas registradas. */
public interface UserRepository {

    User save(User user);

    Optional<User> find(Long id);

    Optional<User> byEmail(String email);

    List<User> all();

    boolean emailExists(String email);

    boolean phoneExists(String telephone);

    boolean documentExists(String document);

    /** Crea el rol de usuaria si la base aún no lo tiene (instalaciones nuevas). */
    void ensureUserRole();
}

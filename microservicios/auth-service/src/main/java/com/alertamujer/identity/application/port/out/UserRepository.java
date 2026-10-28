package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.User;

import java.util.List;
import java.util.Optional;

/** Output port: registered people. */
public interface UserRepository {

    User save(User user);

    Optional<User> find(Long id);

    Optional<User> byEmail(String email);

    List<User> all();

    boolean emailExists(String email);

    boolean phoneExists(String telephone);

    boolean documentExists(String document);

    /** Creates the user role if the database does not have it yet (new installations). */
    void ensureUserRole();
}

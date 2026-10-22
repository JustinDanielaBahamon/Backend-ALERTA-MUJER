package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.User;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: las personas registradas. */
public interface UsuariaRepositorio {

    User save(User usuaria);

    Optional<User> find(Long id);

    Optional<User> porCorreo(String correo);

    List<User> todas();

    boolean existeCorreo(String correo);

    boolean existeTelefono(String telefono);

    boolean existeDocumento(String documento);

    /** Crea el rol de usuaria si la base aún no lo tiene (instalaciones nuevas). */
    void asegurarRolUsuaria();
}

package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.UsuariaRepositorio;
import com.alertamujer.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: las personas en PostgreSQL. El rol se guarda como relación y en el dominio es un id. */
@Component
class UsuariaPersistenceAdapter implements UsuariaRepositorio {

    private final UserJpaRepository jpa;
    private final RoleJpaRepository roles;

    UsuariaPersistenceAdapter(UserJpaRepository jpa, RoleJpaRepository roles) {
        this.jpa = jpa;
        this.roles = roles;
    }

    @Override
    public User guardar(User usuaria) {
        UserJpaEntity f = aFila(usuaria);
        f.setRole(roles.findById(usuaria.getRoleId())
                .orElseThrow(() -> new IllegalStateException("No existe el rol " + usuaria.getRoleId())));
        return aDominio(jpa.save(f));
    }

    @Override
    public Optional<User> buscar(Long id) {
        return jpa.findById(id).map(UsuariaPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<User> porCorreo(String correo) {
        return jpa.findByEmail(correo).map(UsuariaPersistenceAdapter::aDominio);
    }

    @Override
    public List<User> todas() {
        return jpa.findAll().stream().map(UsuariaPersistenceAdapter::aDominio).toList();
    }

    @Override
    public boolean existeCorreo(String correo) {
        return jpa.existsByEmail(correo);
    }

    @Override
    public boolean existeTelefono(String telefono) {
        return jpa.existsByTelephone(telefono);
    }

    @Override
    public boolean existeDocumento(String documento) {
        return jpa.existsByDocumentNumber(documento);
    }

    @Override
    public void asegurarRolUsuaria() {
        if (roles.existsById(User.ROL_USUARIA)) return;
        RoleJpaEntity rol = new RoleJpaEntity();
        rol.setId(User.ROL_USUARIA);
        rol.setName("user");
        rol.setDescription("Usuario estándar de la aplicación");
        roles.save(rol);
    }

    static User aDominio(UserJpaEntity f) {
        User d = new User();
        d.setId(f.getId());
        d.setRoleId(f.getRole() == null ? null : f.getRole().getId());
        d.setRoleName(f.getRole() == null ? null : f.getRole().getName());
        d.setFirstName(f.getFirstName());
        d.setLastName(f.getLastName());
        d.setTelephone(f.getTelephone());
        d.setEmail(f.getEmail());
        d.setDocumentNumber(f.getDocumentNumber());
        d.setDocumentType(f.getDocumentType());
        d.setBirthdate(f.getBirthdate());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    static UserJpaEntity aFila(User d) {
        UserJpaEntity f = new UserJpaEntity();
        f.setId(d.getId());
        f.setFirstName(d.getFirstName());
        f.setLastName(d.getLastName());
        f.setTelephone(d.getTelephone());
        f.setEmail(d.getEmail());
        f.setDocumentNumber(d.getDocumentNumber());
        f.setDocumentType(d.getDocumentType());
        f.setBirthdate(d.getBirthdate());
        f.setCreatedAt(d.getCreatedAt());
        return f;
    }
}

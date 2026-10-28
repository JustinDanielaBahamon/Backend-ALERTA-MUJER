package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.UserRepository;
import com.alertamujer.identity.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: people in PostgreSQL. The role is stored as a relation and in the domain it is an id. */
@Component
class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final RoleJpaRepository roles;

    UserPersistenceAdapter(UserJpaRepository jpa, RoleJpaRepository roles) {
        this.jpa = jpa;
        this.roles = roles;
    }

    @Override
    public User save(User user) {
        UserJpaEntity f = toRow(user);
        f.setRole(roles.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalStateException("No existe el rol " + user.getRoleId())));
        return toDomain(jpa.save(f));
    }

    @Override
    public Optional<User> find(Long id) {
        return jpa.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<User> byEmail(String email) {
        return jpa.findByEmail(email).map(UserPersistenceAdapter::toDomain);
    }

    @Override
    public List<User> all() {
        return jpa.findAll().stream().map(UserPersistenceAdapter::toDomain).toList();
    }

    @Override
    public boolean emailExists(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean phoneExists(String telephone) {
        return jpa.existsByTelephone(telephone);
    }

    @Override
    public boolean documentExists(String document) {
        return jpa.existsByDocumentNumber(document);
    }

    @Override
    public void ensureUserRole() {
        if (roles.existsById(User.USER_ROLE)) return;
        RoleJpaEntity role = new RoleJpaEntity();
        role.setId(User.USER_ROLE);
        role.setName("user");
        role.setDescription("Usuario estándar de la aplicación");
        roles.save(role);
    }

    static User toDomain(UserJpaEntity f) {
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
        d.setTermsAcceptedAt(f.getTermsAcceptedAt());
        d.setTermsVersion(f.getTermsVersion());
        return d;
    }

    static UserJpaEntity toRow(User d) {
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
        f.setTermsAcceptedAt(d.getTermsAcceptedAt());
        f.setTermsVersion(d.getTermsVersion());
        return f;
    }
}

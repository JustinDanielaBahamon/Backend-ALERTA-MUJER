package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.PerfilRepositorio;
import com.alertamujer.identity.domain.model.UserProfile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: los perfiles en PostgreSQL. */
@Component
class PerfilPersistenceAdapter implements PerfilRepositorio {

    private final UserProfileJpaRepository jpa;

    PerfilPersistenceAdapter(UserProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public UserProfile save(UserProfile perfil) {
        return aDominio(jpa.save(aFila(perfil)));
    }

    @Override
    public Optional<UserProfile> find(Long id) {
        return jpa.findById(id).map(PerfilPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<UserProfile> deUsuaria(Long userId) {
        return jpa.findByUserId(userId).map(PerfilPersistenceAdapter::aDominio);
    }

    @Override
    public boolean existeDeUsuaria(Long userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public List<UserProfile> todos() {
        return jpa.findAll().stream().map(PerfilPersistenceAdapter::aDominio).toList();
    }

    static UserProfile aDominio(UserProfileJpaEntity f) {
        UserProfile d = new UserProfile();
        d.setId(f.getId());
        d.setUserId(f.getUserId());
        d.setProfilePhotoUrl(f.getProfilePhotoUrl());
        d.setTutorialCompleted(f.getTutorialCompleted());
        d.setTutorialSeenAt(f.getTutorialSeenAt());
        d.setCreatedAt(f.getCreatedAt());
        d.setUpdatedAt(f.getUpdatedAt());
        return d;
    }

    static UserProfileJpaEntity aFila(UserProfile d) {
        UserProfileJpaEntity f = new UserProfileJpaEntity();
        f.setId(d.getId());
        f.setUserId(d.getUserId());
        f.setProfilePhotoUrl(d.getProfilePhotoUrl());
        f.setTutorialCompleted(d.getTutorialCompleted());
        f.setTutorialSeenAt(d.getTutorialSeenAt());
        f.setCreatedAt(d.getCreatedAt());
        f.setUpdatedAt(d.getUpdatedAt());
        return f;
    }
}

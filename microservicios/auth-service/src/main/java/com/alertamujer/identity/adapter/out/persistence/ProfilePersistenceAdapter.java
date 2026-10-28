package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.ProfileRepository;
import com.alertamujer.identity.domain.model.UserProfile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Output adapter: profiles in PostgreSQL. */
@Component
class ProfilePersistenceAdapter implements ProfileRepository {

    private final UserProfileJpaRepository jpa;

    ProfilePersistenceAdapter(UserProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return toDomain(jpa.save(toRow(profile)));
    }

    @Override
    public Optional<UserProfile> find(Long id) {
        return jpa.findById(id).map(ProfilePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<UserProfile> ofUser(Long userId) {
        return jpa.findByUserId(userId).map(ProfilePersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsForUser(Long userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public List<UserProfile> all() {
        return jpa.findAll().stream().map(ProfilePersistenceAdapter::toDomain).toList();
    }

    static UserProfile toDomain(UserProfileJpaEntity f) {
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

    static UserProfileJpaEntity toRow(UserProfile d) {
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

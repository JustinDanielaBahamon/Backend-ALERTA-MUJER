package com.alertamujer.identity.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, Long> {

    Optional<UserProfileJpaEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}

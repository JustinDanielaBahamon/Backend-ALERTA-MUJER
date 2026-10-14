package com.alertamujer.identity.infrastructure.repository;

import com.alertamujer.identity.domain.model.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
    Optional<AdminProfile> findByAccountId(Long accountId);
    boolean existsByAccountId(Long accountId);
}

package com.alertamujer.identity.infrastructure.repository;

import com.alertamujer.identity.domain.model.RecoveryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecoveryRequestRepository extends JpaRepository<RecoveryRequest, Long> {
    Optional<RecoveryRequest> findByTokenHash(String tokenHash);
    List<RecoveryRequest> findByAccountIdAndStatus(Long accountId, String status);
}

package com.alertamujer.admin.infrastructure.repository;

import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountDeletionRequestRepository extends JpaRepository<AccountDeletionRequest, Long> {
    List<AccountDeletionRequest> findAllByOrderByCreatedAtDesc();

    boolean existsByUserIdAndStatus(Long userId, String status);
}

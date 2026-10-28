package com.alertamujer.admin.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AccountDeletionRequestJpaRepository extends JpaRepository<AccountDeletionRequestJpaEntity, Long> {

    List<AccountDeletionRequestJpaEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByUserIdAndStatus(Long userId, String status);
}

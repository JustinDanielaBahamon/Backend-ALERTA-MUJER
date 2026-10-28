package com.alertamujer.identity.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface RecoveryRequestJpaRepository extends JpaRepository<RecoveryRequestJpaEntity, Long> {

    boolean existsByTokenHash(String tokenHash);

    List<RecoveryRequestJpaEntity> findByAccountIdAndStatus(Long accountId, String status);
}

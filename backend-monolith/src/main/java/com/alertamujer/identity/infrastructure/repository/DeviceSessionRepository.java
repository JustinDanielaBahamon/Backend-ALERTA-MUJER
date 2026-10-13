package com.alertamujer.identity.infrastructure.repository;

import com.alertamujer.identity.domain.model.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {
    Optional<DeviceSession> findByRefreshTokenHash(String refreshTokenHash);
    List<DeviceSession> findByAccountIdAndStatus(Long accountId, String status);
}

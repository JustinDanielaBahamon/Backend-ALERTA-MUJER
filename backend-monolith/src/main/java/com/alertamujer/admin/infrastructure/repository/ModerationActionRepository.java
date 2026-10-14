package com.alertamujer.admin.infrastructure.repository;

import com.alertamujer.admin.domain.model.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationAction, Long> {
    List<ModerationAction> findByUserReportId(Long userReportId);
}

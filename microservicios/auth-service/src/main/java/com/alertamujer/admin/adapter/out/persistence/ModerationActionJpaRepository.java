package com.alertamujer.admin.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

interface ModerationActionJpaRepository extends JpaRepository<ModerationActionJpaEntity, Long> {

    List<ModerationActionJpaEntity> findByUserReportIdIn(Collection<Long> userReportIds);
}

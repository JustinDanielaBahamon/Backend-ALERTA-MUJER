package com.alertamujer.admin.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface UserReportJpaRepository extends JpaRepository<UserReportJpaEntity, Long> {

    List<UserReportJpaEntity> findByReportedUserId(Long reportedUserId);

    List<UserReportJpaEntity> findByStatus(String status);
}

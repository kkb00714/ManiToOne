package com.finalproject.manitoone.repository;

import com.finalproject.manitoone.constants.ReportObjectType;
import com.finalproject.manitoone.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

  boolean existsByTypeAndReportObjectId(ReportObjectType type, Long reportObjectId);
}

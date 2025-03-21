package com.p4th.backend.repository;

import com.p4th.backend.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminReportRepository extends JpaRepository<Report, String>, JpaSpecificationExecutor<Report> {
}

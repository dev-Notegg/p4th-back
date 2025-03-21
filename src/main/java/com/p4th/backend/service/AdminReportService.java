package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Report;
import com.p4th.backend.domain.ReportType;
import com.p4th.backend.dto.response.report.ReportDetailResponse;
import com.p4th.backend.dto.response.report.ReportListResponse;
import com.p4th.backend.mapper.ReportMapper;
import com.p4th.backend.repository.AdminReportRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final AdminReportRepository reportRepository;
    private final ReportMapper reportMapper;

    /**
     * 신고 목록 조회
     * - reporterId(신고자ID), targetUserId(신고대상ID), type(POST/COMMENT)로 검색 가능
     */
    @Transactional(readOnly = true)
    public Page<ReportListResponse> getReports(String reporterId,
                                               String targetUserId,
                                               ReportType type,
                                               Pageable pageable) {
        Specification<Report> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (type != null) {
                p = cb.and(p, cb.equal(root.get("type"), type));
            }
            if (reporterId != null && !reporterId.trim().isEmpty()) {
                p = cb.and(p, cb.equal(root.get("reporterId"), reporterId));
            }
            if (targetUserId != null && !targetUserId.trim().isEmpty()) {
                p = cb.and(p, cb.equal(root.get("targetUserId"), targetUserId));
            }
            return p;
        };

        Page<Report> reportPage = reportRepository.findAll(spec, pageable);

        return reportPage.map(ReportListResponse::from);
    }

    /**
     * 신고 상세 조회
     * - 조회 시 readYn이 0이면 1로 변경하고 readAt 갱신
     */
    @Transactional
    public ReportDetailResponse getReportDetail(String reportId) {
        Report report = reportMapper.getReportById(reportId);
        if (report == null) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }
        // 미확인 상태라면 확인 처리
        if (report.getReadYn() == 0) {
            report.setReadYn(1);
            report.setReadAt(LocalDateTime.now());
            reportRepository.save(report);
        }

        return ReportDetailResponse.from(report);
    }
}

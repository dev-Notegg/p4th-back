package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Report;
import com.p4th.backend.domain.ReportType;
import com.p4th.backend.dto.request.ReportRequest;
import com.p4th.backend.mapper.ReportMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportMapper reportMapper;

    @Transactional
    public String report(String reporterId, ReportRequest reportRequest){
        Report report = new Report();
        report.setReportId(ULIDUtil.getULID());
        report.setReporterId(reporterId);

        if (ReportType.POST.equals(reportRequest.getTargetType()) || ReportType.COMMENT.equals(reportRequest.getTargetType())) {
            report.setTargetId(reportRequest.getTargetId());
            report.setType(reportRequest.getTargetType());
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 신고 대상 타입입니다.");
        }
        report.setTargetUserId(reportRequest.getTargetUserId());
        report.setReason(reportRequest.getReason());
        int inserted = reportMapper.insertReport(report);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "신고 실패");
        }
        return report.getReportId();
    }
}

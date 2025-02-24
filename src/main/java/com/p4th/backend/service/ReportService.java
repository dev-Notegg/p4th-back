package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Report;
import com.p4th.backend.domain.ReportType;
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
    public String reportPost(String postId, String reporterId, String reason) {
        Report report = new Report();
        report.setReportId(ULIDUtil.getULID());
        report.setReporterId(reporterId);
        report.setTargetBoardId(postId);
        report.setTargetUserId(null);
        report.setTargetCommentId(null);
        report.setType(ReportType.POST);
        report.setReason(reason);
        int inserted = reportMapper.insertReport(report);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 신고 실패");
        }
        return report.getReportId();
    }

    @Transactional
    public String reportComment(String commentId, String reporterId, String reason) {
        Report report = new Report();
        report.setReportId(ULIDUtil.getULID());
        report.setReporterId(reporterId);
        report.setTargetCommentId(commentId);
        report.setTargetBoardId(null);
        report.setTargetUserId(null);
        report.setType(ReportType.COMMENT);
        report.setReason(reason);
        int inserted = reportMapper.insertReport(report);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "댓글 신고 실패");
        }
        return report.getReportId();
    }
}

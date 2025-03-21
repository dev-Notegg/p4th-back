package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.domain.ReportType;
import com.p4th.backend.dto.response.report.ReportDetailResponse;
import com.p4th.backend.dto.response.report.ReportListResponse;
import com.p4th.backend.security.Authorization;
import com.p4th.backend.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고 관리 API", description = "게시글/댓글 신고 관리 관련 API")
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final Authorization authorization;

    @Operation(summary = "신고 목록 조회", description = "신고 목록을 조회하며, 신고자ID, 신고대상ID, 신고타입(POST/COMMENT)으로 검색 가능.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportListResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public Page<ReportListResponse> getReports(
            @Parameter(description = "신고 타입(POST, COMMENT)")
            @RequestParam(value = "type", required = false) ReportType type,
            @Parameter(description = "신고자 ID 검색")
            @RequestParam(value = "reporterId", required = false) String reporterId,
            @Parameter(description = "신고 대상 사용자 ID 검색")
            @RequestParam(value = "targetUserId", required = false) String targetUserId,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request
    ) {
        authorization.checkAdmin(request);
        return adminReportService.getReports(reporterId, targetUserId, type, pageable);
    }

    @Operation(summary = "신고 상세 조회", description = "신고 상세 정보를 조회한다. 처음 조회 시 readYn=0이면 1로 변경 처리.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportDetailResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "신고 내역 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{reportId}")
    public ReportDetailResponse getReportDetail(
            @Parameter(description = "신고 ID") @PathVariable String reportId,
            HttpServletRequest request
    ) {
        authorization.checkAdmin(request);
        return adminReportService.getReportDetail(reportId);
    }
}

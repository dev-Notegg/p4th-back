package com.p4th.backend.controller;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.dto.request.ReportRequest;
import com.p4th.backend.dto.response.block.BlockResponse;
import com.p4th.backend.dto.response.report.ReportResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.BlockService;
import com.p4th.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고/차단 API", description = "신고/차단 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportBlockController {

    private final JwtProvider jwtProvider;
    private final ReportService reportService;
    private final BlockService blockService;

    @Operation(summary = "게시글 신고", description = "게시글 신고 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "게시글 신고 성공",
                content = @Content(schema = @Schema(implementation = ReportResponse.class))),
        @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "게시글 신고 중 내부 서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/report/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponse> reportPost(
            @Parameter(name = "postId", description = "신고할 게시글 ID", required = true)
            @PathVariable("postId") String postId,
            @RequestBody ReportRequest reportRequest,
            HttpServletRequest request) {
        String reporterId = jwtProvider.resolveUserId(request);
        if (reporterId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        String reportId = reportService.reportPost(postId, reporterId, reportRequest.getReason());
        ReportResponse response = new ReportResponse(reportId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 신고", description = "댓글 신고 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 신고 성공",
                content = @Content(schema = @Schema(implementation = ReportResponse.class))),
        @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "댓글 신고 중 내부 서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/report/comment/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponse> reportComment(
            @Parameter(name = "commentId", description = "신고할 댓글 ID", required = true)
            @PathVariable("commentId") String commentId,
            @RequestBody ReportRequest reportRequest,
            HttpServletRequest request) {
        String reporterId = jwtProvider.resolveUserId(request);
        if (reporterId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        String reportId = reportService.reportComment(commentId, reporterId, reportRequest.getReason());
        ReportResponse response = new ReportResponse(reportId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "작성자 차단", description = "작성자 차단 API. 해당 작성자를 차단하면 게시글과 댓글 목록 조회 시 해당 작성자의 글이 제외된다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "작성자 차단 성공",
                content = @Content(schema = @Schema(implementation = BlockResponse.class))),
        @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "작성자 차단 중 내부 서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/block/{targetUserId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BlockResponse> blockUser(
            @Parameter(name = "targetUserId", description = "차단할 사용자 ID", required = true)
            @PathVariable("targetUserId") String targetUserId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        String blockId = blockService.blockUser(userId, targetUserId);
        BlockResponse response = new BlockResponse(blockId);
        return ResponseEntity.ok(response);
    }
}

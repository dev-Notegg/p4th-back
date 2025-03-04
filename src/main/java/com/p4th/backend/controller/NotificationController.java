package com.p4th.backend.controller;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.dto.response.NotificationResponse;
import com.p4th.backend.dto.response.UnreadCountResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "알림 API", description = "알림 관련 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NotificationResponse>> getNotifications(HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        List<NotificationResponse> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 처리한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
            @ApiResponse(responseCode = "403", description = "로그인 후 이용가능한 메뉴",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "알림 읽음 처리 중 내부 서버 오류",
                    content = @Content(schema = @Schema(implementation = com.p4th.backend.dto.response.ErrorResponse.class)))
    })
    @PutMapping(value = "/{notificationId}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> markNotificationAsRead(
            @Parameter(name = "notificationId", description = "알림 ID", required = true)
            @PathVariable("notificationId") String notificationId,
            HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "사용자의 읽지 않은 알림 개수를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "읽지 않은 알림 개수 조회 성공",
                    content = @Content(schema = @Schema(implementation = UnreadCountResponse.class)))
    })
    @GetMapping(value = "/unread-count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UnreadCountResponse> getUnreadCount(HttpServletRequest request) {
        String userId = jwtProvider.resolveUserId(request);
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(response);
    }
}

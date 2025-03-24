package com.p4th.backend.controller;

import com.p4th.backend.common.exception.ErrorResponse;
import com.p4th.backend.dto.response.admin.BlockUserResponse;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.AdminBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 차단 관리 API", description = "회원 차단 관리 관련 API")
@RestController
@RequestMapping("/api/admin/blocks")
@RequiredArgsConstructor
public class AdminBlockController {

    private final AdminBlockService adminBlockService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원 차단 관리 목록 조회", 
               description = "전체 회원 목록에서 IP, 회원ID, 닉네임으로 검색하며, 'blockedOnly' 플래그에 따라 차단된 회원만 필터링할 수 있다.")
    @GetMapping
    public ResponseEntity<Page<BlockUserResponse>> getBlockList(
            @Parameter(description = "IP 주소 검색")
            @RequestParam(value="ip", required=false) String ip,
            @Parameter(description = "회원 ID 검색")
            @RequestParam(value="userId", required=false) String userId,
            @Parameter(description = "닉네임 검색")
            @RequestParam(value="nickname", required=false) String nickname,
            @Parameter(description = "차단된 회원만 조회할 경우 true")
            @RequestParam(value="blockedOnly", defaultValue="false") boolean blockedOnly,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BlockUserResponse> page = adminBlockService.getBlockList(ip, userId, nickname, blockedOnly, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "회원 차단", 
               description = "특정 회원을 차단한다. 해당 회원의 상태를 BLOCKED로 변경하고, 두번째 이상 동일 IP 차단인 경우 IP도 차단한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "이미 차단 회원인 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{userId}/block")
    public ResponseEntity<Void> blockUser(
            @PathVariable("userId") String userId,
            HttpServletRequest request) {
        String currentUserId = jwtProvider.resolveUserId(request);
        adminBlockService.blockUser(userId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 차단 해제", 
               description = "특정 회원의 차단을 해제한다. 해당 회원의 상태를 ACTIVE로 변경하고, IP 차단도 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "이미 활성 상태 회원인 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{userId}/unblock")
    public ResponseEntity<Void> unblockUser(
            @PathVariable("userId") String userId,
            HttpServletRequest request) {
        String currentUserId = jwtProvider.resolveUserId(request);
        adminBlockService.unblockUser(userId, currentUserId);
        return ResponseEntity.ok().build();
    }
}

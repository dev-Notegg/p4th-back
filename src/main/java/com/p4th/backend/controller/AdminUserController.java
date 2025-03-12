package com.p4th.backend.controller;

import com.p4th.backend.dto.request.AdminRoleUpdateRequest;
import com.p4th.backend.dto.request.MembershipLevelUpdateRequest;
import com.p4th.backend.dto.response.ErrorResponse;
import com.p4th.backend.dto.response.admin.UserListResponse;
import com.p4th.backend.dto.response.user.UserProfileResponse;
import com.p4th.backend.service.AdminUserService;
import com.p4th.backend.security.Authorization;
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

@Tag(name = "회원 관리 API", description = "회원 관리 관련 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final Authorization authorization;

    @Operation(summary = "회원 목록 조회", description = "회원목록을 조회하며, 회원ID 또는 닉네임으로 검색 가능.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserListResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<UserProfileResponse>> getUsers(
            @Parameter(name = "userId", description = "검색할 회원ID (옵션)")
            @RequestParam(value = "userId", required = false) String userId,
            @Parameter(name = "nickname", description = "검색할 닉네임 (옵션)")
            @RequestParam(value = "nickname", required = false) String nickname,
            HttpServletRequest request,
            @Parameter(hidden = true) @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        authorization.checkAdmin(request);
        Page<UserProfileResponse> users = adminUserService.getUserList(userId, nickname, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "회원 등급 변경", description = "특정 회원의 회원 등급을 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 등급 변경 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{userId}/membership-level")
    public void updateMembershipLevel(
            @PathVariable("userId") String userId,
            @RequestBody MembershipLevelUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        authorization.checkAdmin(httpRequest);
        adminUserService.updateMembershipLevel(userId, request.getMembershipLevel());
    }

    @Operation(summary = "관리자 권한 변경", description = "특정 회원의 관리자 권한을 변경한다. (0=해제, 1=설정)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관리자 권한 변경 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{userId}/admin-role")
    public void updateAdminRole(
            @PathVariable("userId") String userId,
            @RequestBody AdminRoleUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        authorization.checkAdmin(httpRequest);
        adminUserService.updateAdminRole(userId, request.getAdminRole());
    }
}

package com.p4th.backend.security;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.mapper.AdminUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Authorization {

    private final JwtProvider jwtProvider;
    private final AdminUserMapper adminUserMapper;

    /**
     * 관리자 권한 체크
     * 1) 로그인 여부 확인 (JWT에서 userId 추출)
     * 2) userId가 adminRole == 1 인지 확인
     */
    public void checkAdmin(HttpServletRequest request) {
        String currentUserId = jwtProvider.resolveUserId(request);
        if (currentUserId == null) {
            // 로그인 안 된 경우
            throw new CustomException(ErrorCode.LOGIN_REQUIRED, "관리자 권한이 필요합니다.");
        }
        boolean isAdmin = isAdmin(currentUserId);
        if (!isAdmin) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "관리자 권한이 없습니다.");
        }
    }

    /**
     * userId로 관리자의 admin_role을 조회해서 ==1 인지 판단
     */
    public boolean isAdmin(String userId) {
        Integer role = adminUserMapper.findAdminRoleByUserId(userId);
        return role != null && role == 1;
    }
}

package com.p4th.backend.interceptor;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.mapper.AdminUserMapper;
import com.p4th.backend.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    private final AdminUserMapper adminUserMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String currentUserId = jwtProvider.resolveUserId(request);
        if (currentUserId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        Integer role = adminUserMapper.findAdminRoleByUserId(currentUserId);
        if (role == null || role != 1) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS, "관리자 권한이 없습니다.");
        }
        return true;
    }
}

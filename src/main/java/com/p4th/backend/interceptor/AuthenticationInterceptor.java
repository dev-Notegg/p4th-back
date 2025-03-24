package com.p4th.backend.interceptor;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.AccountStatus;
import com.p4th.backend.domain.User;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.AdminBlockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import com.p4th.backend.util.IpUtil;

@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    private final AuthMapper authMapper;
    private final AdminBlockService adminBlockService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // JWT 토큰에서 userId 추출
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            return true;
        }

        // 사용자 상태 체크 (BLOCKED인 경우 차단)
        User user = authMapper.selectByUserId(userId);
        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new CustomException(ErrorCode.BLOCKED_USER, "관리자로 인해 차단된 회원입니다.");
        }

        // 로그인 요청 IP 체크 (차단된 IP이면 접근 불가)
        String ip = IpUtil.extractClientIp(request);
        if (adminBlockService.isIpBlocked(ip)) {
            throw new CustomException(ErrorCode.BLOCKED_IP, "관리자로 인해 차단된 IP 주소입니다.");
        }

        return true;
    }
}

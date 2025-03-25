package com.p4th.backend.aspect;

import com.p4th.backend.annotation.RequireLogin;
import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.AccountStatus;
import com.p4th.backend.domain.User;
import com.p4th.backend.mapper.AuthMapper;
import com.p4th.backend.security.JwtProvider;
import com.p4th.backend.service.AdminBlockService;
import com.p4th.backend.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginCheckAspect {

    private final JwtProvider jwtProvider;
    private final AuthMapper authMapper;
    private final AdminBlockService adminBlockService;

    @Around("@annotation(requireLogin)")
    public Object checkLogin(ProceedingJoinPoint joinPoint, @SuppressWarnings("unused") RequireLogin requireLogin) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        HttpServletRequest request = attributes.getRequest();
        String userId = jwtProvider.resolveUserId(request);
        if (userId == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        User user = authMapper.selectByUserId(userId);
        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new CustomException(ErrorCode.BLOCKED_USER);
        }
        String ip = IpUtil.extractClientIp(request);
        if (adminBlockService.isIpBlocked(ip)) {
            throw new CustomException(ErrorCode.BLOCKED_IP);
        }
        // userId를 request에 저장 (컨트롤러에서 재사용)
        request.setAttribute("currentUserId", userId);

        return joinPoint.proceed();
    }
}

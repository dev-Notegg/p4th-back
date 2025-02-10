package com.p4th.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.p4th.backend.domain.User;

import javax.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${p4th.jwt.secret}")
    private String secretKey;

    private Key hmacKey;

    private static final long ACCESS_TOKEN_EXPIRE_MS = 5 * 60 * 1000;  // 5분
    private static final long REFRESH_TOKEN_EXPIRE_MS = 365L * 24 * 60 * 60 * 1000;  // 1년

    @PostConstruct
    public void init() {
        hmacKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 회원정보(User 객체)를 이용하여 Access Token 생성 (추가 클레임 포함)
    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + ACCESS_TOKEN_EXPIRE_MS);

        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("user_id", user.getUserId())
                .claim("nickname", user.getNickname())
                .claim("membership_level", user.getMembershipLevel())
                .claim("admin_role", user.getAdminRole())
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(hmacKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + REFRESH_TOKEN_EXPIRE_MS);

        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("user_id", user.getUserId())
                .claim("nickname", user.getNickname())
                .claim("membership_level", user.getMembershipLevel())
                .claim("admin_role", user.getAdminRole())
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(hmacKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰 유효성 검증 (예외 발생 시 로깅 추가)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(hmacKey).build().parseClaimsJws(token);
            return true;
        } catch(Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // JWT 토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(hmacKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 추가: HttpServletRequest에서 Authorization 헤더를 분석하여 토큰에서 userId를 추출하는 메서드
    public String resolveUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null) {
            header = header.trim();
            // "Bearer " 접두사가 있으면 제거하고 토큰 추출
            if (header.startsWith("Bearer ")) {
                String token = header.substring("Bearer ".length()).trim();
                return getUserIdFromToken(token);
            }
            // 만약 접두사가 없다면, 그대로 토큰으로 간주
            return getUserIdFromToken(header);
        }
        return null;
    }
}

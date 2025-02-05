package com.p4th.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.p4th.backend.domain.User;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${p4th.jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRE_MS = 5 * 60 * 1000;  // 5분
    private static final long REFRESH_TOKEN_EXPIRE_MS = 365L * 24 * 60 * 60 * 1000;  // 1년

    // 회원정보(User 객체)를 이용하여 Access Token 생성 (추가 클레임 포함)
    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + ACCESS_TOKEN_EXPIRE_MS);
        Key hmacKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("login_id", user.getLoginId())
                .claim("nickname", user.getNickname())
                .claim("membership_level", user.getMembershipLevel())
                .claim("admin_role", user.getAdminRole())
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(hmacKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 단순히 userId를 이용하여 Refresh Token 생성
    public String generateRefreshToken(String userId) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + REFRESH_TOKEN_EXPIRE_MS);
        Key hmacKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(hmacKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).build().parseClaimsJws(token);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}

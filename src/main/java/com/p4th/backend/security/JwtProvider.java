package com.p4th.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${p4th.jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRE_MS = 5 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRE_MS = 365L * 24 * 60 * 60 * 1000;

    public String generateAccessToken(String userId) {
        return generateToken(userId, ACCESS_TOKEN_EXPIRE_MS);
    }

    public String generateRefreshToken(String userId) {
        return generateToken(userId, REFRESH_TOKEN_EXPIRE_MS);
    }

    private String generateToken(String userId, long expireMillis) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + expireMillis);

        Key hmacKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date(now))
                .setExpiration(expiry)
                .signWith(hmacKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

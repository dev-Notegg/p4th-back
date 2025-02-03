package com.p4th.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 비밀번호 해시
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 비밀번호 매칭
    public static boolean matches(String rawPassword, String hashed) {
        return encoder.matches(rawPassword, hashed);
    }
}

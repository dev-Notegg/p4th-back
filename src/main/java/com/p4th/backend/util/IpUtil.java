package com.p4th.backend.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {
    public static String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            // X-Forwarded-For can contain multiple IPs, take the first one.
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

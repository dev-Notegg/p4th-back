package com.p4th.backend.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RelativeTimeFormatter {

    /**
     * 주어진 LocalDateTime(createdAt)과 현재 시간의 차이를 계산하여,
     * 1분 미만이면 "0분 전", 1분 이상 60분 미만이면 "XX분 전", 60분 이상 24시간 미만이면 "XX시간 전",
     * 24시간 이상이면 "yyyy-MM-dd" 형식의 날짜 문자열을 반환한다.
     *
     * @param createdAt 게시글 또는 댓글 생성 시각
     * @return 상대적 시간 문자열
     */
    public static String formatRelativeTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);
        long minutes = duration.toMinutes();
        long hours = duration.toHours();

        if (minutes < 1) {
            return "0분 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return createdAt.format(formatter);
        }
    }
}

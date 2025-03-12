package com.p4th.backend.controller;

import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostHistoryLog;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.p4th.backend.util.ULIDUtil;

@Component
@RequiredArgsConstructor
public class SchedulerController {

    private final PostMapper postMapper;
    private final PostHistoryLogMapper postHistoryLogMapper;

    private float calculatePopularity(Post post) {
        return post.getViewCount() * 0.4f + post.getCommentCount() * 0.2f;
    }

    private void processPopularity(String periodType, String periodStart, String periodEnd) {
        List<Post> posts = postMapper.getAllPosts();

        // 모든 게시글의 인기도 점수를 계산한 후, 내림차순으로 정렬하여 상위 20개 선택
        List<Post> topPosts = posts.stream()
                .sorted((p1, p2) -> Float.compare(calculatePopularity(p2), calculatePopularity(p1)))
                .limit(20)
                .toList();

        for (Post post : topPosts) {
            float popularityScore = calculatePopularity(post);
            PostHistoryLog log = new PostHistoryLog();
            log.setHistoryId(ULIDUtil.getULID());
            log.setPostId(post.getPostId());
            log.setPeriodType(periodType);
            log.setPeriodStartDate(periodStart);
            log.setPeriodEndDate(periodEnd);
            log.setViewCount(post.getViewCount());
            log.setCommentCount(post.getCommentCount());
            log.setPopularityScore(popularityScore);
            postHistoryLogMapper.insertHistoryLog(log);
        }
    }

    // DAILY: 매일 00:05에 실행 (전날 기준)
    @Scheduled(cron = "0 5 0 * * *")
    public void scheduleDailyPopularity() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        processPopularity("DAILY", yesterday.toString(), yesterday.toString());
    }

    // WEEKLY: 매주 월요일 00:05에 실행 (전 주 기준)
    @Scheduled(cron = "0 5 0 * * MON")
    public void scheduleWeeklyPopularity() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate sunday = monday.plusDays(6);
        processPopularity("WEEKLY", monday.toString(), sunday.toString());
    }

    // MONTHLY: 매월 1일 00:05에 실행 (전월 기준)
    @Scheduled(cron = "0 5 0 1 * *")
    public void scheduleMonthlyPopularity() {
        LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
        processPopularity("MONTHLY", firstDayOfLastMonth.toString(), lastDayOfLastMonth.toString());
    }

    // HOURLY: 매 시간 정각에 실행 (지난 1시간 기준)
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleHourlyPopularity() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastHourStart = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lastHourEnd = lastHourStart.plusHours(1).minusNanos(1);
        processPopularity("HOURLY", lastHourStart.toString(), lastHourEnd.toString());
    }

    // 유저마다 최근 본 게시글이 16개 이상인 경우 삭제
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupPostViews() {
        // 모든 사용자 ID 조회
        List<String> userIds = postMapper.getDistinctUserIdsFromPostView();
        for (String userId : userIds) {
            // 해당 사용자의 16번째 최신 조회일시를 구함
            LocalDateTime cutoff = postMapper.get16thLatestViewedAt(userId);
            if (cutoff != null) {
                // cutoff보다 이전의 모든 post_view 레코드 삭제
                postMapper.deletePostViewsOlderThan(userId, cutoff);
            }
        }
    }
}

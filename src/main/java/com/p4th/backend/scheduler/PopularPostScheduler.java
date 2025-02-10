package com.p4th.backend.scheduler;

import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostHistoryLog;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PopularPostScheduler {

    private final PostMapper postMapper;
    private final PostHistoryLogMapper postHistoryLogMapper;

    // DAILY: 매일 00:05에 실행 (전날 기준)
    @Scheduled(cron = "0 5 0 * * *")
    public void scheduleDailyPopularity() {
        List<Post> posts = postMapper.getAllPosts();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String periodStart = yesterday.toString();
        String periodEnd = yesterday.toString();
        for (Post post : posts) {
            float popularityScore = post.getViewCount() * 0.4f + post.getCommentCount() * 0.2f;
            PostHistoryLog log = new PostHistoryLog();
            log.setHistoryId(UUID.randomUUID().toString());
            log.setPostId(post.getPostId());
            log.setPeriodType("DAILY");
            log.setPeriodStartDate(periodStart);
            log.setPeriodEndDate(periodEnd);
            log.setViewCount(post.getViewCount());
            log.setCommentCount(post.getCommentCount());
            log.setPopularityScore(popularityScore);
            log.setCreatedBy("SYSTEM");
            postHistoryLogMapper.insertHistoryLog(log);
        }
    }

    // WEEKLY: 매주 월요일 00:05에 실행 (전 주 기준)
    @Scheduled(cron = "0 5 0 * * MON")
    public void scheduleWeeklyPopularity() {
        List<Post> posts = postMapper.getAllPosts();
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY).minusWeeks(1); // 전 주 월요일
        LocalDate sunday = monday.plusDays(6); // 전 주 일요일
        String periodStart = monday.toString();
        String periodEnd = sunday.toString();
        for (Post post : posts) {
            float popularityScore = post.getViewCount() * 0.4f + post.getCommentCount() * 0.2f;
            PostHistoryLog log = new PostHistoryLog();
            log.setHistoryId(UUID.randomUUID().toString());
            log.setPostId(post.getPostId());
            log.setPeriodType("WEEKLY");
            log.setPeriodStartDate(periodStart);
            log.setPeriodEndDate(periodEnd);
            log.setViewCount(post.getViewCount());
            log.setCommentCount(post.getCommentCount());
            log.setPopularityScore(popularityScore);
            log.setCreatedBy("SYSTEM");
            postHistoryLogMapper.insertHistoryLog(log);
        }
    }

    // MONTHLY: 매월 1일 00:05에 실행 (전월 기준)
    @Scheduled(cron = "0 5 0 1 * *")
    public void scheduleMonthlyPopularity() {
        List<Post> posts = postMapper.getAllPosts();
        LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
        String periodStart = firstDayOfLastMonth.toString();
        String periodEnd = lastDayOfLastMonth.toString();
        for (Post post : posts) {
            float popularityScore = post.getViewCount() * 0.4f + post.getCommentCount() * 0.2f;
            PostHistoryLog log = new PostHistoryLog();
            log.setHistoryId(UUID.randomUUID().toString());
            log.setPostId(post.getPostId());
            log.setPeriodType("MONTHLY");
            log.setPeriodStartDate(periodStart);
            log.setPeriodEndDate(periodEnd);
            log.setViewCount(post.getViewCount());
            log.setCommentCount(post.getCommentCount());
            log.setPopularityScore(popularityScore);
            log.setCreatedBy("SYSTEM");
            postHistoryLogMapper.insertHistoryLog(log);
        }
    }
}

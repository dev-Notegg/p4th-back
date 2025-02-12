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
import com.p4th.backend.util.ULIDUtil;

@Component
@RequiredArgsConstructor
public class PopularPostScheduler {

    private final PostMapper postMapper;
    private final PostHistoryLogMapper postHistoryLogMapper;

    private float calculatePopularity(Post post) {
        return post.getViewCount() * 0.4f + post.getCommentCount() * 0.2f;
    }

    private void processPopularity(String periodType, String periodStart, String periodEnd) {
        List<Post> posts = postMapper.getAllPosts();
        for (Post post : posts) {
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
            log.setCreatedBy("SYSTEM");
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
}

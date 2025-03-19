package com.p4th.backend.controller;

import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.PostHistoryLog;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.p4th.backend.util.ULIDUtil;

@Component
@RequiredArgsConstructor
public class SchedulerController {

    private final PostMapper postMapper;
    private final PostHistoryLogMapper postHistoryLogMapper;

    // 기간 내 집계된 조회수와 댓글 수를 기준으로 인기 점수 계산
    private float calculatePopularity(int viewCount, int commentCount) {
        return viewCount * 0.4f + commentCount * 0.2f;
    }

    private void processPopularity(String periodType, LocalDateTime periodStart, LocalDateTime periodBoundary) {
        List<Post> posts = postMapper.getAllPosts();

        // 모든 게시글에 대해 해당 기간 내 집계 수치를 가져와 인기 점수를 계산
        List<PostPopularity> popularityList = posts.stream().map(post -> {
            int periodViewCount = postMapper.getViewCountWithinPeriod(post.getPostId(), periodStart, periodBoundary);
            int periodCommentCount = postMapper.getCommentCountWithinPeriod(post.getPostId(), periodStart, periodBoundary);
            float popularityScore = calculatePopularity(periodViewCount, periodCommentCount);
            return new PostPopularity(post, popularityScore, periodViewCount, periodCommentCount);
        }).toList();

        // 인기 점수 내림차순 정렬 후 상위 20개 선택
        List<PostPopularity> topPosts = popularityList.stream()
                .sorted((pp1, pp2) -> Float.compare(pp2.popularityScore, pp1.popularityScore))
                .limit(20)
                .toList();

        // 인기 게시글 로그 저장
        for (PostPopularity pp : topPosts) {
            PostHistoryLog log = new PostHistoryLog();
            log.setHistoryId(ULIDUtil.getULID());
            log.setPostId(pp.post.getPostId());
            log.setPeriodType(periodType);
            log.setPeriodStartDate(periodStart.withNano(0));
            log.setViewCount(pp.periodViewCount);
            log.setCommentCount(pp.periodCommentCount);
            log.setPopularityScore(pp.popularityScore);
            postHistoryLogMapper.insertHistoryLog(log);
        }
    }

    // HOURLY: 매 시간 정각에 실행 (지난 1시간 기준)
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleHourlyPopularity() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastHourStart = now.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime nextHourStart = lastHourStart.plusHours(1);
        processPopularity("HOURLY", lastHourStart, nextHourStart);
    }

    // DAILY: 매일 00:05에 실행 (전날 기준)
    @Scheduled(cron = "0 5 0 * * *")
    public void scheduleDailyPopularity() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime dayStart = yesterday.atStartOfDay();
        LocalDateTime nextDayStart = dayStart.plusDays(1);
        processPopularity("DAILY", dayStart, nextDayStart);
    }

    // WEEKLY: 매주 월요일 00:05에 실행 (전 주 기준)
    @Scheduled(cron = "0 5 0 * * MON")
    public void scheduleWeeklyPopularity() {
        LocalDate lastMonday = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        LocalDateTime weekStart = lastMonday.atStartOfDay();
        LocalDateTime nextWeekStart = weekStart.plusWeeks(1);
        processPopularity("WEEKLY", weekStart, nextWeekStart);
    }

    // MONTHLY: 매월 1일 00:05에 실행 (전월 기준)
    @Scheduled(cron = "0 5 0 1 * *")
    public void scheduleMonthlyPopularity() {
        LocalDate firstDayLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDateTime monthStart = firstDayLastMonth.atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);
        processPopularity("MONTHLY", monthStart, nextMonthStart);
    }

    // 생성된 지 2달 이상된 post_view 데이터를 삭제
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupPostViews() {
        // 현재 시각에서 2달 이전의 시각 계산
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(2);
        // 모든 post_view 레코드 중, cutoff보다 이전에 생성된 데이터 삭제
        postMapper.deletePostViewsOlderThan(cutoff);
    }

    // 내부 클래스: 게시글과 해당 기간의 집계 결과 보관
    private static class PostPopularity {
        Post post;
        float popularityScore;
        int periodViewCount;
        int periodCommentCount;

        public PostPopularity(Post post, float popularityScore, int periodViewCount, int periodCommentCount) {
            this.post = post;
            this.popularityScore = popularityScore;
            this.periodViewCount = periodViewCount;
            this.periodCommentCount = periodCommentCount;
        }
    }
}

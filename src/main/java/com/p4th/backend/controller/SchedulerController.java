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

    // 기간 내 집계된 조회수와 댓글 수를 기준으로 인기 점수 계산
    private float calculatePopularity(int viewCount, int commentCount) {
        return viewCount * 0.4f + commentCount * 0.2f;
    }

    private void processPopularity(String periodType, String periodStart, String periodEnd) {
        List<Post> posts = postMapper.getAllPosts();

        // 모든 게시글에 대해 기간 내 집계된 수치를 가져와 인기 점수를 계산
        List<PostPopularity> popularityList = posts.stream().map(post -> {
            int periodViewCount = postMapper.getViewCountWithinPeriod(post.getPostId(), periodStart, periodEnd);
            int periodCommentCount = postMapper.getCommentCountWithinPeriod(post.getPostId(), periodStart, periodEnd);
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
            log.setPeriodStartDate(periodStart);
            log.setPeriodEndDate(periodEnd);
            // 기간 내 집계된 수치를 로그에 기록
            log.setViewCount(pp.periodViewCount);
            log.setCommentCount(pp.periodCommentCount);
            log.setPopularityScore(pp.popularityScore);
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

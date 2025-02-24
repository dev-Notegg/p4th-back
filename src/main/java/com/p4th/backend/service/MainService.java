package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Banner;
import com.p4th.backend.dto.response.board.PopularBoardResponse;
import com.p4th.backend.dto.response.post.PopularPostResponse;
import com.p4th.backend.dto.response.post.PostListResponse;
import com.p4th.backend.mapper.MainMapper;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.util.RelativeTimeFormatter;
import com.p4th.backend.util.HtmlContentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MainService {

    private final MainMapper mainMapper;
    private final PostHistoryLogMapper postHistoryLogMapper;

    public List<PopularBoardResponse> getPopularBoards() {
        try {
            List<PopularBoardResponse> boards = mainMapper.getPopularBoards();
            if (boards == null) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인기 게시판 목록 조회 실패");
            }
            return boards;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인기 게시판 목록 조회 중 오류: " + e.getMessage());
        }
    }

    public List<Banner> getBanners() {
        try {
            List<Banner> banners = mainMapper.getBanners();
            if (banners == null) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "배너 목록 조회 실패");
            }
            return banners;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "배너 목록 조회 중 오류: " + e.getMessage());
        }
    }

    public List<PopularPostResponse> getPopularPosts(String period, String userId) {
        try {
            // 현재 날짜 기준으로 조회 기간 계산 (전날, 전주, 전달)
            LocalDate today = LocalDate.now();
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("period", period);

            if ("DAILY".equalsIgnoreCase(period)) {
                LocalDate yesterday = today.minusDays(1);
                params.put("startDate", yesterday.toString());
                params.put("endDate", yesterday.toString());
            } else if ("WEEKLY".equalsIgnoreCase(period)) {
                // 전주: 지난 주 월요일 ~ 일요일
                LocalDate lastMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                LocalDate lastSunday = lastMonday.plusDays(6);
                params.put("startDate", lastMonday.toString());
                params.put("endDate", lastSunday.toString());
            } else if ("MONTHLY".equalsIgnoreCase(period)) {
                // 전달: 지난 달 1일 ~ 마지막 날
                LocalDate firstDayLastMonth = today.minusMonths(1).withDayOfMonth(1);
                LocalDate lastDayLastMonth = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());
                params.put("startDate", firstDayLastMonth.toString());
                params.put("endDate", lastDayLastMonth.toString());
            } else {
                throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 조회 기간입니다.");
            }

            List<PopularPostResponse> responses = postHistoryLogMapper.getPopularPostsByPeriod(params);
            responses.forEach(this::processPopularPostResponse);
            return responses;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인기 게시글 조회 중 오류: " + e.getMessage());
        }
    }

    /**
     * 각 PopularPostResponse 객체에 대해 공통 처리 로직
     */
    private void processPopularPostResponse(PopularPostResponse response) {
        // 이미지 처리
        String imgUrl = PostListResponse.extractFirstImageUrl(response.getContent());
        int imgCount = PostListResponse.countInlineImages(response.getContent());
        response.setImageUrl(imgUrl);
        response.setImageCount(imgCount);
        // HTML 태그 제거 후 순수 텍스트 추출 (최대 50자)
        if (response.getContent() != null && !response.getContent().isEmpty()) {
            String plainText = HtmlContentUtils.extractPlainText(response.getContent(), 50);
            response.setContent(plainText);
        }
        // 제목 최대 23자 처리
        if (response.getTitle() != null) {
            response.setTitle(HtmlContentUtils.extractText(response.getTitle(), 23));
        }
        // 생성일시를 상대 시간 형식으로 변환
        if (response.getCreatedAt() != null && !response.getCreatedAt().isEmpty()) {
            LocalDateTime createdTime = LocalDateTime.parse(response.getCreatedAt(), PostListResponse.formatter);
            response.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(createdTime));
        }
    }
}

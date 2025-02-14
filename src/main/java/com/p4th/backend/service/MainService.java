package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Banner;
import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.dto.response.board.PopularBoardResponse;
import com.p4th.backend.dto.response.post.PopularPostResponse;
import com.p4th.backend.mapper.MainMapper;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {

    private final MainMapper mainMapper;
    private final PostHistoryLogMapper postHistoryLogMapper;
    private static final DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<PopularBoardResponse> getPopularBoards() {
        try {
            List<PopularBoardResponse> boards = mainMapper.getPopularBoards();
            if (boards == null) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인기 게시판 목록 조회 실패");
            }
            return boards;
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
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "배너 목록 조회 중 오류: " + e.getMessage());
        }
    }

    public List<PopularPostResponse> getPopularPosts(String period) {
        try {
            List<PopularPostResponse> responses = postHistoryLogMapper.getPopularPostsByPeriod(period);
            responses.forEach(response -> {
                try {
                    // imageUrl, imageCount는 content 필드에서 추출
                    //삭제된 게시글인 경우 이미지 처리하지 않음
                    if (response.getContent() != null && !response.getContent().isEmpty() &&
                            !PostStatus.DELETED.equals(response.getStatus())) {
                        String imgUrl = HtmlImageUtils.extractFirstImageUrl(response.getContent());
                        int imgCount = HtmlImageUtils.countInlineImages(response.getContent());
                        response.setImageUrl(imgUrl);
                        response.setImageCount(imgCount);
                    }
                    // content를 HTML 태그 제거 후 순수 텍스트로 변환하고, 최대 30자까지 자르기
                    if (response.getContent() != null && !response.getContent().isEmpty()) {
                        String plainText = Jsoup.parse(response.getContent()).text();
                        if (plainText.length() > 30) {
                            plainText = plainText.substring(0,30);
                        }
                        response.setContent(plainText);
                    }
                    if (response.getCreatedAt() != null && !response.getCreatedAt().isEmpty()) {
                        LocalDateTime createdTime = LocalDateTime.parse(response.getCreatedAt(), originalFormatter);
                        response.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(createdTime));
                    }
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인기 게시글 처리 중 오류: " + e.getMessage());
                }
            });
            return responses;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "인기 게시글 조회 중 오류: " + e.getMessage());
        }
    }
}

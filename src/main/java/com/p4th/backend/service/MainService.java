package com.p4th.backend.service;

import com.p4th.backend.domain.Banner;
import com.p4th.backend.domain.PostStatus;
import com.p4th.backend.dto.response.board.PopularBoardResponse;
import com.p4th.backend.dto.response.post.PopularPostResponse;
import com.p4th.backend.mapper.MainMapper;
import com.p4th.backend.mapper.PostHistoryLogMapper;
import com.p4th.backend.util.HtmlImageUtils;
import com.p4th.backend.util.RelativeTimeFormatter;
import lombok.RequiredArgsConstructor;
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
        return mainMapper.getPopularBoards();
    }

    public List<Banner> getBanners() {
        return mainMapper.getBanners();
    }

    public List<PopularPostResponse> getPopularPosts(String period) {
        List<PopularPostResponse> responses = postHistoryLogMapper.getPopularPostsByPeriod(period);
        responses.forEach(response -> {
            // imageUrl, imageCount는 content 필드에서 추출
            //삭제된 게시글인 경우 이미지 처리하지 않음
            if (response.getContent() != null && !response.getContent().isEmpty() && !PostStatus.DELETED.equals(response.getStatus())) {
                String imgUrl = HtmlImageUtils.extractFirstImageUrl(response.getContent());
                int imgCount = HtmlImageUtils.countInlineImages(response.getContent());
                response.setImageUrl(imgUrl);
                response.setImageCount(imgCount);
            }

            if (response.getCreatedAt() != null && !response.getCreatedAt().isEmpty()) {
                LocalDateTime createdTime = LocalDateTime.parse(response.getCreatedAt(), originalFormatter);
                response.setCreatedAt(RelativeTimeFormatter.formatRelativeTime(createdTime));
            }
        });
        return responses;
    }
}

package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Scrap;
import com.p4th.backend.dto.response.scrap.ScrapPostListResponse;
import com.p4th.backend.mapper.ScrapMapper;
import com.p4th.backend.repository.ScrapRepository;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapMapper scrapMapper;
    private final ScrapRepository scrapRepository;

    @Transactional(readOnly = true)
    public Page<ScrapPostListResponse> getScrapPosts(String userId, String scrapFolderId, Pageable pageable) {
        try {
            Page<Scrap> scraps;
            if (scrapFolderId == null || scrapFolderId.trim().isEmpty()) {
                scraps = scrapRepository.findByUserId(userId, pageable);
            } else {
                scraps = scrapRepository.findByUserIdAndScrapFolderId(userId, scrapFolderId, pageable);
            }
            return scraps.map(scrap ->
                    ScrapPostListResponse.from(scrap.getPost(), scrap.getScrapId(), scrap.getScrapFolderId())
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 게시글 목록 조회 중 오류: " + e.getMessage());
        }
    }

    @Transactional
    public String deleteScrap(String scrapId, String userId) {
        int deleted = scrapMapper.deleteScrap(scrapId, userId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 스크랩 삭제 실패");
        }
        return scrapId;
    }

    @Transactional
    public String createScrap(String postId, String scrapFolderId, String userId) {
        Scrap scrap = new Scrap();
        String scrapId = ULIDUtil.getULID();
        scrap.setScrapId(scrapId);
        scrap.setUserId(userId);
        scrap.setScrapFolderId(scrapFolderId);
        scrap.setPostId(postId);
        int inserted = scrapMapper.insertScrap(scrap);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 스크랩 실패");
        }
        return scrapId;
    }
}

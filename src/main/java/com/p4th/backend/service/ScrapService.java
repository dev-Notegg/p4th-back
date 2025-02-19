package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Scrap;
import com.p4th.backend.mapper.ScrapMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapMapper scrapMapper;

    @Transactional(readOnly = true)
    public List<Scrap> getScraps(String userId, String scrapFolderId) {
        if (scrapFolderId == null || scrapFolderId.trim().isEmpty()) {
            return scrapMapper.getScrapsByUserId(userId);
        } else {
            return scrapMapper.getScrapsByFolderId(userId, scrapFolderId);
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

package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Post;
import com.p4th.backend.domain.Scrap;
import com.p4th.backend.domain.ScrapFolder;
import com.p4th.backend.dto.response.scrap.ScrapPostListResponse;
import com.p4th.backend.mapper.PostMapper;
import com.p4th.backend.mapper.ScrapFolderMapper;
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
    private final PostMapper postMapper;
    private final ScrapFolderMapper scrapFolderMapper;

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
        // 스크랩 데이터 존재 여부 확인 (해당 사용자 소유여야 함)
        Scrap scrap = scrapMapper.getScrapById(scrapId, userId);
        if (scrap == null) {
            throw new CustomException(ErrorCode.SCRAP_NOT_FOUND);
        }
        int deleted = scrapMapper.deleteScrap(scrapId, userId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 스크랩 삭제 실패");
        }
        return scrapId;
    }

    @Transactional
    public String createScrap(String postId, String scrapFolderId, String userId) {
        // 게시글 존재 여부 체크
        Post post = postMapper.getPostDetail(postId);
        if (post == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        // scrapFolderId가 제공되었다면 해당 폴더 존재 여부 체크 (userId 기준)
        if (scrapFolderId != null && !scrapFolderId.trim().isEmpty()) {
            ScrapFolder folder = scrapFolderMapper.selectScrapFolderById(scrapFolderId, userId);
            if (folder == null) {
                throw new CustomException(ErrorCode.SCRAP_FOLDER_NOT_FOUND);
            }
        }

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

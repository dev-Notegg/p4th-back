package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Scrap;
import com.p4th.backend.domain.ScrapFolder;
import com.p4th.backend.mapper.ScrapFolderMapper;
import com.p4th.backend.mapper.ScrapMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapFolderMapper scrapFolderMapper;
    private final ScrapMapper scrapMapper;

    // 스크랩 폴더 목록 조회 (사용자 ID 기준)
    @Transactional(readOnly = true)
    public List<ScrapFolder> getScrapFolders(String userId) {
        return scrapFolderMapper.getScrapFoldersByUserId(userId);
    }

    // 스크랩 폴더 생성
    @Transactional
    public ScrapFolder createScrapFolder(String userId, String folderName) {
        if(folderName == null || folderName.trim().isEmpty()){
            throw new CustomException(ErrorCode.INVALID_INPUT, "폴더명은 빈값일 수 없습니다.");
        }
        // 해당 사용자의 기존 폴더 중 최대 sort_order 값을 조회하여 새 폴더의 순서를 결정
        Integer maxSortOrder = scrapFolderMapper.getMaxSortOrderByUserId(userId);
        int newSortOrder = (maxSortOrder == null) ? 0 : maxSortOrder + 1;

        ScrapFolder folder = new ScrapFolder();
        folder.setScrapFolderId(ULIDUtil.getULID());
        folder.setUserId(userId);
        folder.setFolderName(folderName);
        folder.setSortOrder(newSortOrder);
        folder.setCreatedBy(userId);
        int inserted = scrapFolderMapper.insertScrapFolder(folder);
        if(inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더 생성 실패");
        }
        return folder;
    }

    // 스크랩 폴더명 변경
    @Transactional
    public ScrapFolder updateScrapFolderName(String scrapFolderId, String newFolderName, String userId) {
        if(newFolderName == null || newFolderName.trim().isEmpty()){
            throw new CustomException(ErrorCode.INVALID_INPUT, "폴더명은 빈값일 수 없습니다.");
        }
        ScrapFolder folder = new ScrapFolder();
        folder.setScrapFolderId(scrapFolderId);
        folder.setFolderName(newFolderName);
        folder.setUpdatedBy(userId);
        int updated = scrapFolderMapper.updateScrapFolder(folder);
        if(updated != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더명 변경 실패");
        }
        return folder;
    }

    // 스크랩 폴더 순서 변경
    @Transactional
    public boolean updateScrapFolderOrder(List<String> order, String userId) {
        for (int i = 0; i < order.size(); i++) {
            int updated = scrapFolderMapper.updateScrapFolderOrder(order.get(i), i, userId);
            if (updated != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더 순서 변경 실패");
            }
        }
        return true;
    }

    // 스크랩 폴더 삭제
    @Transactional
    public boolean deleteScrapFolder(String scrapFolderId) {
        int deleted = scrapFolderMapper.deleteScrapFolder(scrapFolderId);
        if(deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더 삭제 실패");
        }
        return true;
    }

    // 게시글 스크랩 목록 조회 (사용자 기준)
    @Transactional(readOnly = true)
    public List<Scrap> getScraps(String userId) {
        return scrapMapper.getScrapsByUserId(userId);
    }

    // 게시글 스크랩 삭제
    @Transactional
    public boolean deleteScrap(String scrapId) {
        int deleted = scrapMapper.deleteScrap(scrapId);
        if(deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 스크랩 삭제 실패");
        }
        return true;
    }

    // 게시글 스크랩 (게시글 스크랩 생성)
    @Transactional
    public Scrap createScrap(String postId, String scrapFolderId) {
        Scrap scrap = new Scrap();
        scrap.setScrapId(ULIDUtil.getULID());
        scrap.setPostId(postId);
        scrap.setScrapFolderId(scrapFolderId);
        scrap.setScrappedAt(LocalDateTime.now());
        int inserted = scrapMapper.insertScrap(scrap);
        if(inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게시글 스크랩 실패");
        }
        return scrap;
    }
}

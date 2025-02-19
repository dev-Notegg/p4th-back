package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.ScrapFolder;
import com.p4th.backend.mapper.ScrapFolderMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapFolderService {

    private final ScrapFolderMapper scrapFolderMapper;

    @Transactional(readOnly = true)
    public List<ScrapFolder> getScrapFolders(String userId) {
        return scrapFolderMapper.getScrapFoldersByUserId(userId);
    }

    @Transactional
    public ScrapFolder createScrapFolder(String userId, String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "폴더명은 빈값일 수 없습니다.");
        }
        Integer maxSortOrder = scrapFolderMapper.getMaxSortOrderByUserId(userId);
        int newSortOrder = (maxSortOrder == null || maxSortOrder == -1) ? 0 : maxSortOrder + 1;
        
        ScrapFolder folder = new ScrapFolder();
        folder.setScrapFolderId(ULIDUtil.getULID());
        folder.setUserId(userId);
        folder.setFolderName(folderName);
        folder.setSortOrder(newSortOrder);
        folder.setCreatedAt(LocalDateTime.now());
        
        int inserted = scrapFolderMapper.insertScrapFolder(folder);
        if (inserted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더 생성 실패");
        }
        return folder;
    }

    @Transactional
    public ScrapFolder updateScrapFolderName(String scrapFolderId, String newFolderName, String userId) {
        if (newFolderName == null || newFolderName.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "폴더명은 빈값일 수 없습니다.");
        }
        ScrapFolder folder = new ScrapFolder();
        folder.setScrapFolderId(scrapFolderId);
        folder.setFolderName(newFolderName);
        folder.setUserId(userId);

        int updated = scrapFolderMapper.updateScrapFolder(folder);
        if (updated != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더명 변경 실패");
        }
        return folder;
    }

    @Transactional
    public boolean updateScrapFolderOrder(List<String> orderedFolderIds, String userId) {
        for (int i = 0; i < orderedFolderIds.size(); i++) {
            String folderId = orderedFolderIds.get(i);
            int updated = scrapFolderMapper.updateScrapFolderOrder(folderId, i, userId);
            if (updated != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더 순서 변경 실패");
            }
        }
        return true;
    }

    @Transactional
    public boolean deleteScrapFolder(String scrapFolderId, String userId) {
        int deleted = scrapFolderMapper.deleteScrapFolder(scrapFolderId, userId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더 삭제 실패");
        }
        return true;
    }
}

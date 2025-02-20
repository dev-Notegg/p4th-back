package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.ScrapFolder;
import com.p4th.backend.dto.response.scrap.ScrapFolderResponse;
import com.p4th.backend.mapper.ScrapFolderMapper;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapFolderService {

    private final ScrapFolderMapper scrapFolderMapper;

    @Transactional(readOnly = true)
    public List<ScrapFolderResponse> getScrapFolders(String userId) {
        List<ScrapFolder> folders = scrapFolderMapper.getScrapFoldersByUserId(userId);
        return folders.stream()
                .map(folder -> {
                    int scrapCount = scrapFolderMapper.countByScrapFolderId(folder.getScrapFolderId());
                    return ScrapFolderResponse.fromWithCount(folder, scrapCount);
                })
                .collect(Collectors.toList());
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
    public boolean updateScrapFolderName(String scrapFolderId, String newFolderName, String userId) {
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
        return true;
    }

    @Transactional
    public boolean updateScrapFolderOrder(List<String> orderedFolderIds, String userId) {
        // DB에서 해당 유저의 모든 스크랩 폴더 ID 조회 (오름차순 정렬)
        List<String> allFolderIds = scrapFolderMapper.findScrapFolderIdsByUser(userId);

        // 요청된 폴더 개수와 DB의 폴더 개수가 다르면 오류 처리
        if (orderedFolderIds.size() != allFolderIds.size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "모든 스크랩 폴더의 순서를 지정해주세요.");
        }

        // 요청된 폴더 목록과 DB 목록이 동일한지 (순서와 상관없이) 확인
        if (!new HashSet<>(orderedFolderIds).equals(new HashSet<>(allFolderIds))) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "요청된 스크랩 폴더 목록이 올바르지 않습니다.");
        }

        // 새 순서에 따라 각 폴더의 sort_order 업데이트
        for (int i = 0; i < orderedFolderIds.size(); i++) {
            String scrapFolderId = orderedFolderIds.get(i);
            int updated = scrapFolderMapper.updateScrapFolderOrder(scrapFolderId, i, userId);
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
        // 삭제 후 남은 스크랩 폴더들을 조회하여 sort_order를 재설정 (오름차순 정렬)
        List<ScrapFolder> remainingFolders = scrapFolderMapper.findScrapFoldersByUser(userId);
        remainingFolders.sort(Comparator.comparingInt(ScrapFolder::getSortOrder));
        for (int i = 0; i < remainingFolders.size(); i++) {
            ScrapFolder folder = remainingFolders.get(i);
            int updated = scrapFolderMapper.updateScrapFolderOrder(folder.getScrapFolderId(), i, userId);
            if (updated != 1) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "스크랩 폴더 순서 재정렬 실패");
            }
        }
        return true;
    }
}

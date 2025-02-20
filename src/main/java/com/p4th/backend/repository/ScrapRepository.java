package com.p4th.backend.repository;

import com.p4th.backend.domain.Scrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<Scrap, String> {
    // 스크랩폴더 필터가 없는 경우
    Page<Scrap> findByUserId(String userId, Pageable pageable);

    // 스크랩폴더 필터가 있는 경우
    Page<Scrap> findByUserIdAndScrapFolderId(String userId, String scrapFolderId, Pageable pageable);
}

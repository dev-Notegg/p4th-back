package com.p4th.backend.repository;

import com.p4th.backend.domain.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminBannerRepository extends JpaRepository<Banner, String> {
    // 검색: 광고식별명 기준 LIKE 검색 (필터가 없으면 전체 조회)
    Page<Banner> findByBannerNameContainingIgnoreCase(String bannerName, Pageable pageable);
}

package com.p4th.backend.service;

import com.p4th.backend.common.exception.CustomException;
import com.p4th.backend.common.exception.ErrorCode;
import com.p4th.backend.domain.Banner;
import com.p4th.backend.dto.response.admin.BannerResponse;
import com.p4th.backend.mapper.AdminBannerMapper;
import com.p4th.backend.repository.AdminBannerRepository;
import com.p4th.backend.util.ULIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBannerService {

    private final AdminBannerRepository adminBannerRepository;
    private final AdminBannerMapper adminBannerMapper;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Page<BannerResponse> getBanners(String search, Pageable pageable) {
        Page<Banner> page;
        if (search == null || search.trim().isEmpty()) {
            page = adminBannerRepository.findAll(pageable);
        } else {
            page = adminBannerRepository.findByBannerNameContainingIgnoreCase(search, pageable);
        }
        return page.map(BannerResponse::from);
    }

    @Transactional
    public String createBanner(String userId, String bannerName, String linkUrl,
            LocalDate startDate, LocalDate endDate, MultipartFile imageFile) {
        // 1) ULID 생성
        String bannerId = ULIDUtil.getULID();
        // 2) 이미지 업로드 (S3)
        String fileName = ULIDUtil.getULID() + "_" + imageFile.getOriginalFilename();
        String imageUrl = s3Service.upload(imageFile, "banners", fileName);
        // 3) 광고 종료일이 현재보다 과거라면 예외 처리
        if(endDate.isBefore(LocalDate.now())){
            throw new CustomException(ErrorCode.INVALID_INPUT, "광고 종료일이 현재일 이전입니다.");
        }
        // 4) 배너 엔티티 생성
        Banner banner = new Banner();
        banner.setBannerId(bannerId);
        banner.setBannerName(bannerName);
        banner.setImageUrl(imageUrl);
        banner.setLinkUrl(linkUrl);
        banner.setStartDate(startDate);
        banner.setEndDate(endDate);
        banner.setCreatedBy(userId);
        // 5) 마지막 seq 값 부여
        int maxSeq = adminBannerMapper.findMaxSeqForActiveBanners();
        banner.setSeq(maxSeq + 1);
        // 6) DB Insert
        adminBannerMapper.insertBanner(banner);
        return bannerId;
    }

    @Transactional
    public void deleteBanner(String bannerId) {
        Banner banner = adminBannerMapper.findById(bannerId);
        if (banner == null) {
            throw new CustomException(ErrorCode.BANNER_NOT_FOUND);
        }
        adminBannerMapper.deleteBanner(bannerId);
        s3Service.deleteByFileUrl(banner.getImageUrl());
        adminBannerMapper.deleteBanner(bannerId);
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBanners() {
        List<Banner> activeBanners = adminBannerMapper.selectActiveBanners();
        return activeBanners.stream()
                .map(BannerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateActiveBannerOrder(List<String> order) {
        for (int i = 0; i < order.size(); i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("bannerId", order.get(i));
            params.put("seq", i + 1);
            adminBannerMapper.updateBannerSeq(params);
        }
    }
}

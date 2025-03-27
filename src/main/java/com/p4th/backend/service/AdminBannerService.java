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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBannerService {

    private final AdminBannerRepository adminBannerRepository;
    private final AdminBannerMapper adminBannerMapper;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public Page<BannerResponse> getBanners(String search, Pageable pageable) {
        Page<Banner> page = (search == null || search.trim().isEmpty())
                ? adminBannerRepository.findAll(pageable)
                : adminBannerRepository.findByBannerNameContainingIgnoreCase(search, pageable);
        return page.map(BannerResponse::from);
    }

    @Transactional
    public String createBanner(String userId, String bannerName, String linkUrl,
                               LocalDate startDate, LocalDate endDate, MultipartFile imageFile) {
        // 1) 공통 입력값 검증
        validateBannerInput(bannerName, startDate, endDate, imageFile);

        // 2) 종료일 검증 (현재 날짜 이전이면 오류)
        if (endDate.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "광고 종료일이 현재일 이전입니다.");
        }

        // 3) ULID 생성 및 이미지 업로드
        String bannerId = ULIDUtil.getULID();
        String fileName = ULIDUtil.getULID() + "_" + imageFile.getOriginalFilename();
        String imageUrl = s3Service.upload(imageFile, "banners", fileName);

        // 4) 배너 엔티티 생성 및 DB 저장
        Banner banner = new Banner();
        banner.setBannerId(bannerId);
        banner.setBannerName(bannerName);
        banner.setImageUrl(imageUrl);
        banner.setLinkUrl(linkUrl);
        banner.setStartDate(startDate);
        banner.setEndDate(endDate);
        banner.setCreatedBy(userId);
        int maxSeq = adminBannerMapper.findMaxSeqForActiveBanners();
        banner.setSeq(maxSeq + 1);
        adminBannerMapper.insertBanner(banner);
        return bannerId;
    }

    // 입력값 검증
    private void validateBannerInput(String bannerName, LocalDate startDate, LocalDate endDate, MultipartFile imageFile) {
        if (bannerName == null || bannerName.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "배너 이름은 필수입니다.");
        }
        if (startDate == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "시작일은 필수입니다.");
        }
        if (endDate == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "종료일은 필수입니다.");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "이미지 파일은 필수입니다.");
        }
    }

    @Transactional
    public void deleteBanner(String bannerId) {
        Banner banner = adminBannerMapper.findById(bannerId);
        if (banner == null) {
            throw new CustomException(ErrorCode.BANNER_NOT_FOUND);
        }
        adminBannerMapper.deleteBanner(bannerId);
        s3Service.deleteByFileUrl(banner.getImageUrl());
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBanners() {
        List<Banner> activeBanners = adminBannerMapper.selectActiveBanners();
        return activeBanners.stream()
                .map(BannerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateActiveBannerOrder(List<String> order, String userId) {
        for (int i = 0; i < order.size(); i++) {
            adminBannerMapper.updateBannerSeq(order.get(i), userId, i + 1);
        }
    }
}

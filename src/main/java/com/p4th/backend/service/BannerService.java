package com.p4th.backend.service;

import com.p4th.backend.domain.Banner;
import com.p4th.backend.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerMapper bannerMapper;

    public List<Banner> getBanners() {
        return bannerMapper.getBanners();
    }
}

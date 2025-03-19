package com.p4th.backend.mapper;

import com.p4th.backend.domain.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BannerMapper {
    Banner findById(@Param("bannerId") String bannerId);

    int insertBanner(Banner banner);

    int deleteBanner(@Param("bannerId") String bannerId);

    List<Banner> selectActiveBanners();

    void updateBannerSeq(Map<String, Object> params);

    int findMaxSeqForActiveBanners();
}

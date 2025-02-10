package com.p4th.backend.mapper;

import com.p4th.backend.domain.Banner;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BannerMapper {
    List<Banner> getBanners();
}

package com.p4th.backend.mapper;

import com.p4th.backend.domain.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface BannerMapper {
    @Select("SELECT banner_id, banner_name, image_url, link_url, display_yn, seq, start_date, end_date FROM banner ORDER BY seq")
    List<Banner> getBanners();
}

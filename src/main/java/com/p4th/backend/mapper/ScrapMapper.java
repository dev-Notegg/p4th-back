package com.p4th.backend.mapper;

import com.p4th.backend.domain.Scrap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScrapMapper {
    int insertScrap(Scrap scrap);
    int deleteScrap(@Param("scrapId") String scrapId, @Param("userId") String userId);
}

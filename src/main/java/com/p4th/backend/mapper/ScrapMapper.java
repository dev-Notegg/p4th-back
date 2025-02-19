package com.p4th.backend.mapper;

import com.p4th.backend.domain.Scrap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScrapMapper {
    List<Scrap> getScrapsByUserId(@Param("userId") String userId);
    List<Scrap> getScrapsByFolderId(@Param("userId") String userId, @Param("scrapFolderId") String scrapFolderId);
    int insertScrap(Scrap scrap);
    int deleteScrap(@Param("scrapId") String scrapId);
}

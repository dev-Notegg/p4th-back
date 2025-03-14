package com.p4th.backend.mapper;

import com.p4th.backend.domain.ScrapFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScrapFolderMapper {
    List<ScrapFolder> getScrapFoldersByUserId(@Param("userId") String userId);
    int insertScrapFolder(ScrapFolder folder);
    int updateScrapFolder(ScrapFolder folder);
    int updateScrapFolderOrder(@Param("scrapFolderId") String scrapFolderId, @Param("sortOrder") int sortOrder, @Param("userId") String userId);
    int deleteScrapFolder(@Param("scrapFolderId") String scrapFolderId, @Param("userId") String userId);
    Integer getMaxSortOrderByUserId(@Param("userId") String userId);
    ScrapFolder getDefaultScrapFolderByUserId(@Param("userId") String userId);
    List<String> findScrapFolderIdsByUser(@Param("userId") String userId);
    List<ScrapFolder> findScrapFoldersByUser(@Param("userId") String userId);
    int countByScrapFolderId(@Param("scrapFolderId") String scrapFolderId);
    ScrapFolder selectScrapFolderById(@Param("scrapFolderId") String scrapFolderId, @Param("userId") String userId);
}

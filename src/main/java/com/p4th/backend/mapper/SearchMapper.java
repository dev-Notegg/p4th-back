package com.p4th.backend.mapper;

import com.p4th.backend.dto.SearchResponse.SearchResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SearchMapper {
    List<SearchResult> searchPosts(@Param("query") String query);
}

package com.p4th.backend.mapper;

import com.p4th.backend.dto.response.PopularPostResponse;
import com.p4th.backend.domain.PostHistoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostHistoryLogMapper {
    void insertHistoryLog(PostHistoryLog log);
    List<PopularPostResponse> getPopularPostsByPeriod(@Param("period") String period);
}

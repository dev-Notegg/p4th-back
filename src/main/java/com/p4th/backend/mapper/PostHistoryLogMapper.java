package com.p4th.backend.mapper;

import com.p4th.backend.dto.response.post.PopularPostResponse;
import com.p4th.backend.domain.PostHistoryLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface PostHistoryLogMapper {
    void insertHistoryLog(PostHistoryLog log);
    List<PopularPostResponse> getPopularPostsByPeriod(Map<String, Object> params);
}

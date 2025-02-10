package com.p4th.backend.mapper;

import com.p4th.backend.dto.PopularBoardResponse;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BoardMapper {
    List<PopularBoardResponse> getPopularBoards();
}

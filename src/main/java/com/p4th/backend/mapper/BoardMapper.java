package com.p4th.backend.mapper;

import com.p4th.backend.domain.Board;
import com.p4th.backend.dto.response.PopularBoardResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<PopularBoardResponse> getPopularBoards();
    List<Board> getBoardsByCategory(@Param("categoryId") String categoryId);
}

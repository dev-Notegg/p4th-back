package com.p4th.backend.mapper;

import com.p4th.backend.domain.Banner;
import com.p4th.backend.dto.response.board.PopularBoardResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MainMapper {
    List<PopularBoardResponse> getPopularBoards();
    List<Banner> getBanners();
}
